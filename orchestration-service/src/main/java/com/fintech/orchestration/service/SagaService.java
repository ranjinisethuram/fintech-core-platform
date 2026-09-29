package com.fintech.orchestration.service;

import com.fintech.common.domain.SagaContextType;
import com.fintech.common.event.*;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.fraudcontract.dto.HistoricalTransaction;
import com.fintech.fraudcontract.dto.HistoricalTransactionBuilder;
import com.fintech.orchestration.domain.*;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.feign.transaction.TransactionQueryAdapter;
import com.fintech.orchestration.outbox.SagaOutbox;
import com.fintech.orchestration.outbox.SagaOutboxRepository;
import com.fintech.orchestration.repository.SagaContextRepository;
import com.fintech.orchestration.repository.SagaErrorRepository;
import com.fintech.orchestration.repository.SagaRepository;
import com.fintech.outbox.OutboxMapper;
import com.fintech.transactioncontract.dto.TransactionHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;
    private final SagaOutboxRepository sagaOutboxRepository;
    private final OutboxMapper outboxMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;
    private final SagaContextMapper sagaContextMapper;
    private final SagaContextRepository sagaContextRepository;
    private final SagaErrorRepository sagaErrorRepository;
    private final TransactionQueryAdapter transactionQueryAdapter;

    @Value("${saga.retry.limit:3}")
    private int retryLimit;

    public SagaService(SagaRepository sagaRepository, SagaOutboxRepository sagaOutboxRepository,
                       OutboxMapper outboxMapper, MessageEnvelopeFactory messageEnvelopeFactory, SagaContextMapper sagaContextMapper, SagaContextRepository sagaContextRepository, SagaErrorRepository sagaErrorRepository, TransactionQueryAdapter transactionQueryAdapter) {
        this.sagaRepository = sagaRepository;
        this.sagaOutboxRepository = sagaOutboxRepository;
        this.outboxMapper = outboxMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
        this.sagaContextMapper = sagaContextMapper;
        this.sagaContextRepository = sagaContextRepository;
        this.sagaErrorRepository = sagaErrorRepository;
        this.transactionQueryAdapter = transactionQueryAdapter;
    }

    public Saga startSaga(String correlationId, String aggregateId
            //, SagaState initialState,
                          //SagaState nextState
    ){
        boolean isSagaExists = sagaRepository.existsByAggregateId(aggregateId);
        if(isSagaExists)
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_ALREADY_AVAILABLE);
        Saga saga = Saga.start(correlationId,aggregateId);
        return sagaRepository.save(saga);
    }

    public Saga updateSagaState(Saga sagaToUpdate, StepId currentStep){
        sagaToUpdate.moveTo(currentStep);
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaComplete(Saga sagaToUpdate, StepId currentStep){
        sagaToUpdate.moveTo(currentStep);
        sagaToUpdate.complete();
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaFailed(Saga sagaToUpdate, String failureReason){
        sagaToUpdate.fail(failureReason);
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaForRetry(Saga sagaToUpdate){
        Duration backoff = exponentialBackoff(sagaToUpdate.getRetryCount());
        sagaToUpdate.markForRetry(backoff);
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaForCompensation(Saga sagaToUpdate){
        sagaToUpdate.compensating();
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaCompensated(Saga sagaToUpdate){
        sagaToUpdate.compensated();
        return sagaRepository.save(sagaToUpdate);
    }

    public Saga updateSagaCompensationFailed(Saga sagaToUpdate, String failureReason) {
        sagaToUpdate.compensationFailed(failureReason);
        return sagaRepository.save(sagaToUpdate);
    }

    public void createSagaContext(UUID sagaId,
                                  SagaContextType sagaContextType, Object sagaContext){
        boolean isSagaContextExists = this.sagaContextRepository.existsById(sagaId);
        if(isSagaContextExists)
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_ALREADY_AVAILABLE);
        String contextJson = this.sagaContextMapper.toJson(sagaContext);
        SagaContext sagaContextEntity = new SagaContext(sagaId,sagaContextType,contextJson);
        this.sagaContextRepository.save(sagaContextEntity);
    }

    public SagaContext fetchSagaContext(UUID sagaId){
        return this.sagaContextRepository.findById(sagaId).orElse(null);
    }

    public <T> T loadSagaContext(String contextJson, Class<T> clazz){
        return this.sagaContextMapper
                .fromJson(contextJson, clazz);
    }

    public <T> void updateSagaContext(SagaContext sagaContextEntity, T sagaContext){
        String contextJson = this.sagaContextMapper.toJson(sagaContext);
        sagaContextEntity.updateSagaContextJson(contextJson);
        this.sagaContextRepository.save(sagaContextEntity);
    }

    private void persistSagaError(MessageEnvelope<?> messageEnvelope, String failureReason){
        SagaError sagaError = new SagaError(messageEnvelope.getSagaId(), messageEnvelope.getMessageId(),
                messageEnvelope.getAggregateId(), messageEnvelope.getAggregateType(), messageEnvelope.getCorrelationId(),
                outboxMapper.serializeMessageEnvelopePayload(messageEnvelope.getPayload()), failureReason);
        this.sagaErrorRepository.saveAndFlush(sagaError);
    }

    public List<HistoricalTransaction> fetchLast30DaysTransactionHistory(UUID sourceAccountId) {
        List<TransactionHistory> transactionHistoryList = this.transactionQueryAdapter.fetchTransactionHistory(sourceAccountId,
                Instant.now().minus(30, ChronoUnit.DAYS));
        List<HistoricalTransaction> historicalTransactionList = transactionHistoryList.stream().map(transactionHistory ->
                new HistoricalTransaction(
                        transactionHistory.getAmount(),
                        transactionHistory.getTimestamp()
        )).collect(Collectors.toList());
        HistoricalTransactionBuilder historicalTransactionBuilder = new HistoricalTransactionBuilder();
        historicalTransactionBuilder.addTransactions(historicalTransactionList);
        return historicalTransactionBuilder.build();
    }

    @Transactional
    public void handleSagaProcessingException(MessageEnvelope<?> messageEnvelope, ErrorCode errorCode){
        //emit failed event to kafka
        //if saga is avialable, update the status.
        Saga currentSaga = fetchSagaById(UUID.fromString(messageEnvelope.getSagaId()));
        if(currentSaga != null)
            this.updateSagaFailed(currentSaga, errorCode.getErrorMessage());

        persistSagaError(messageEnvelope,errorCode.getErrorMessage());

        OrchestrationErrorEvent orchestrationErrorEvent = new OrchestrationErrorEvent(messageEnvelope.getMessageId(),
                messageEnvelope.getEventType(),messageEnvelope.getSagaId()
                ,messageEnvelope.getAggregateType(),
                messageEnvelope.getAggregateId(),messageEnvelope.getCorrelationId()
                ,errorCode.getErrorMessage(), Instant.now());
        buildMessageEnvelopeAndPushToOutbox(orchestrationErrorEvent, messageEnvelope.getCorrelationId(),
                messageEnvelope.getMessageId(),messageEnvelope.getSagaId(), "orchestration-events");
    }

//    public void publishCreateAccountCommandToOutbox(Saga sagaSaved, MessageEnvelope<?> envelope){
//        UUID accountId = UUID.randomUUID();
//        CustomerCreatedEvent customerCreatedEvent = (CustomerCreatedEvent)
//                envelope.getPayload();
//        CreateAccountCommand createAccountCommand = new CreateAccountCommand(accountId
//                ,UUID.fromString(customerCreatedEvent.customerId()),true);
//        this.buildMessageEnvelopeAndPushToOutbox(createAccountCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                sagaSaved.getSagaId().toString(), false);
//    }
//
//    public void publishCreateWalletCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope){
//        UUID walletId = UUID.randomUUID();
//        AccountCreatedEvent accountCreatedEvent = (AccountCreatedEvent)envelope.getPayload();
//        CreateWalletCommand createWalletCommand = new CreateWalletCommand(walletId,
//                UUID.fromString(accountCreatedEvent.accountId()),
//                UUID.fromString(accountCreatedEvent.customerId()));
//        this.buildMessageEnvelopeAndPushToOutbox(createWalletCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }
//
//    public void publishCreateWalletLedgerAccountCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope){
//        UUID ledgerAccountId = UUID.randomUUID();
//        WalletCreatedEvent walletCreatedEvent = (WalletCreatedEvent) envelope.getPayload();
//        CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand = new CreateWalletLedgerAccountCommand(
//                ledgerAccountId,
//                UUID.fromString(walletCreatedEvent.walletId()),
//                UUID.fromString(walletCreatedEvent.accountId())
//                ,UUID.fromString(walletCreatedEvent.customerId()),
//                walletCreatedEvent.currency());
//        this.buildMessageEnvelopeAndPushToOutbox(createWalletLedgerAccountCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }
//
//    public void handleTransaction(Saga sagaSaved, MessageEnvelope<?> envelope){
//        TransactionInitiatedEvent transactionInitiatedEvent = (TransactionInitiatedEvent)
//                envelope.getPayload();
//        //Send commands based on transaction type
//        switch (transactionInitiatedEvent.transactionType()){
//            case DEPOSIT -> {
//                    /* persist deposit saga context.
//                    send command to ledger-service to create double entries.
//                    PLATFORM_BANK - amount debited
//                    DEST_ACCOUNT - amount credited
//                    Once double entries are created successfully, send command to wallet-service
//                    to credit the wallet associated with dest account.
//                    If error encountered in any step, saga fails and no compensation involved.
//                    */
//
//
////                DepositContext depositContext = new DepositContext();
////                depositContext.setTransactionId(transactionInitiatedEvent.transactionId());
////                depositContext.setAccountId(transactionInitiatedEvent.destinationAccountId());
////                depositContext.setAmount(transactionInitiatedEvent.amount());
////                depositContext.setCurrency(transactionInitiatedEvent.currency());
////                this.createSagaContext(sagaSaved.getSagaId(),SagaContextType.DEPOSIT,depositContext);
//
////                LedgerEntryRequest ledgerEntryRequest = new LedgerEntryRequest(LedgerEntryType.CREDIT,
////                        transactionInitiatedEvent.destinationAccountId());
//                CreateLedgerEntryCommand createLedgerEntryCommand = new CreateLedgerEntryCommand(
//                        UUID.fromString(transactionInitiatedEvent.transactionId()),
//                        null,
//                        UUID.fromString(transactionInitiatedEvent.destinationAccountId()),
//                        transactionInitiatedEvent.amount(),
//                        transactionInitiatedEvent.currency(),
//                        transactionInitiatedEvent.transactionType());
//                publishCreateLedgerEntriesCommand(sagaSaved,envelope,createLedgerEntryCommand);
//            }
//            case WITHDRAWAL -> {
//                    /*Persist withdrwal saga context.
//                    send command to wallet-service to reserver funds.
//                    Once funds are reserved successfully, send command to ledger-service
//                    to create double entries.
//                    SRC_ACCOUNT - amount debited
//                    PAYMENT_CLEARANCE - amount credited
//                    Once double entries are created successfully, send command to wallet-service
//                    to debit the wallet associated with source account.
//                    If any error encountered during ledger entry creation after retries or wallet update,
//                    saga fails and reserved funds are released and transaction will be marked compensated.
//                    If success, reservation status changes to consumed and transaction as success.
//                    If any issue with fund reservation, transaction is marked failed.
//                    * */
//
////                WithdrawalContext withdrawalContext = new WithdrawalContext();
////                withdrawalContext.setTransactionId(transactionInitiatedEvent.transactionId());
////                withdrawalContext.setAccountId(transactionInitiatedEvent.sourceAccountId());
////                withdrawalContext.setAmount(transactionInitiatedEvent.amount());
////                withdrawalContext.setCurrency(transactionInitiatedEvent.currency());
////
////                this.createSagaContext(sagaSaved.getSagaId(),SagaContextType.WITHDRAWAL,withdrawalContext);
//            }
//            case TRANSFER -> {
//                     /*Persist send command to wallet-service to reserve funds from source account.
//                     On success, create double entry in ledger.
//                     SRC_ACCOUNT - amount debited
//                     DEST_ACCOUNT - amount credited
//                     On success, send command to wallet-service to debit amount from source
//                     and credit amount to destination.
//                     If any error encountered during ledger entry creation after retries or wallet update,
//                    saga fails and reserved funds are released and transaction will be marked compensated.
//                    If success, reservation status changes to consumed and transaction as success.
//                    If any issue with fund reservation, transaction is marked failed.
//                     *  */
////                TransferContext transferContext = new TransferContext();
////                transferContext.setTransactionId(transactionInitiatedEvent.transactionId());
////                transferContext.setSourceAccountId(transactionInitiatedEvent.sourceAccountId());
////                transferContext.setDestinationAccountId(transactionInitiatedEvent.destinationAccountId());
////
////                this.createSagaContext(sagaSaved.getSagaId(),SagaContextType.TRANSFER,transferContext);
//            }
//        }
//    }

//    public void publishCreateLedgerEntriesCommand(Saga sagaSaved, MessageEnvelope<?> envelope,
//                                                  CreateLedgerEntryCommand createLedgerEntryCommand){
//
//        this.buildMessageEnvelopeAndPushToOutbox(createLedgerEntryCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                sagaSaved.getSagaId().toString(), false);
//    }
//
//    public void publishActivateAccountCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope){
//        LedgerAccountCreatedEvent ledgerAccountCreatedEvent = (LedgerAccountCreatedEvent) envelope.getPayload();
//        ActivateAccountCommand activateAccountCommand =
//                new ActivateAccountCommand(UUID.fromString(ledgerAccountCreatedEvent.accountId())
//                        ,UUID.fromString(ledgerAccountCreatedEvent.customerId()));
//        this.buildMessageEnvelopeAndPushToOutbox(activateAccountCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }
//
//    public void publishUpdateWalletBalanceCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope) {
////        LedgerEntriesCreatedEvent ledgerEntriesCreatedEvent =
////                (LedgerEntriesCreatedEvent)envelope.getPayload();
////        switch (ledgerEntriesCreatedEvent.transactionType()){
////            case DEPOSIT -> {
////                publishCreditWalletCommandToOutbox(currentSaga,envelope);
////            }
////            case WITHDRAWAL -> {
////                publishDebitWalletCommandToOutbox(currentSaga,envelope);
////            }
////            case TRANSFER -> {
////                publishWalletTransferCommandToOutbox(currentSaga, envelope);
////            }
////        }
//    }

    public Saga fetchSagaById(UUID sagaId){
        Optional<Saga> currentSaga = this.sagaRepository.findById(sagaId);
        return currentSaga.orElse(null);
    }

//    public void publishCreditWalletCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope){
//        LedgerEntriesCreatedEvent ledgerEntriesCreatedEvent =
//                (LedgerEntriesCreatedEvent)envelope.getPayload();
//        CreditWalletCommand creditWalletCommand = new CreditWalletCommand(ledgerEntriesCreatedEvent.toAccount(),
//                UUID.fromString(ledgerEntriesCreatedEvent.transactionId()),
//                ledgerEntriesCreatedEvent.amount(),
//                ledgerEntriesCreatedEvent.currency(),ledgerEntriesCreatedEvent.transactionType());
//        this.buildMessageEnvelopeAndPushToOutbox(creditWalletCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }

//    public void publishDebitWalletCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope){
//        LedgerEntriesCreatedEvent ledgerEntriesCreatedEvent =
//                (LedgerEntriesCreatedEvent)envelope.getPayload();
//        DebitWalletCommand debitWalletCommand = new DebitWalletCommand(ledgerEntriesCreatedEvent.fromAccount(),
//                ledgerEntriesCreatedEvent.transactionId(), ledgerEntriesCreatedEvent.amount(),
//                ledgerEntriesCreatedEvent.currency(),ledgerEntriesCreatedEvent.transactionType());
//        this.buildMessageEnvelopeAndPushToOutbox(debitWalletCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }

//    public void publishWalletTransferCommandToOutbox(Saga currentSaga, MessageEnvelope<?> envelope) {
//        LedgerEntriesCreatedEvent ledgerEntriesCreatedEvent =
//                (LedgerEntriesCreatedEvent)envelope.getPayload();
//        TransferWalletAmountCommand transferWalletAmountCommand = new TransferWalletAmountCommand(
//                ledgerEntriesCreatedEvent.fromAccount(),
//                ledgerEntriesCreatedEvent.toAccount(),
//                UUID.fromString(ledgerEntriesCreatedEvent.transactionId()),
//                ledgerEntriesCreatedEvent.amount(),
//                ledgerEntriesCreatedEvent.currency(),
//                ledgerEntriesCreatedEvent.transactionType());
//        this.buildMessageEnvelopeAndPushToOutbox(transferWalletAmountCommand,
//                envelope.getCorrelationId(),
//                envelope.getMessageId(),
//                currentSaga.getSagaId().toString(), false);
//    }
//
//    public void handleAccountCreationSagaFailure(MessageEnvelope<?> envelope, Saga currentSaga){
//        AccountCreationSagaFailedEvent accountCreationSagaFailedEvent =
//                (AccountCreationSagaFailedEvent)envelope.getPayload();
//        if(accountCreationSagaFailedEvent.retryable() &&
//                isSagaRetryable(currentSaga.getRetryCount())){
//            //mark the saga for retry
//            Duration backoff = exponentialBackoff(currentSaga.getRetryCount());
//            currentSaga.markForRetry(backoff);
//        }else{
//            //mark as failure
//            currentSaga.fail(accountCreationSagaFailedEvent.reason());
//        }
//        this.sagaRepository.save(currentSaga);
//    }
//
//    public void handleTransactionSagaFailure(MessageEnvelope<?> envelope, Saga currentSaga){
//        TransactionSagaFailedEvent transactionSagaFailedEvent =
//                (TransactionSagaFailedEvent) envelope.getPayload();
//        if(transactionSagaFailedEvent.retryable() &&
//                isSagaRetryable(currentSaga.getRetryCount())){
//            //mark the saga for retry
//            Duration backoff = exponentialBackoff(currentSaga.getRetryCount());
//            currentSaga.markForRetry(backoff);
//        }else{
//            //mark as failure
//            currentSaga.fail(transactionSagaFailedEvent.reason());
//        }
//        this.sagaRepository.save(currentSaga);
//    }

    public void publishSagaFailedEvent(MessageEnvelope<?> envelope, Saga saga, String failureReason, boolean isCompensated){
        SagaContext sagaContext = this.fetchSagaContext(saga.getSagaId());
        String sagaContextJson = this.sagaContextMapper.toJson(sagaContext);
        SagaFailedEvent sagaFailedEvent = new SagaFailedEvent(
                    saga.getSagaId().toString(),
                    sagaContextJson,
                    sagaContext.getSagaContextType(),
                    failureReason,
                    isCompensated,
                    Instant.now()
            );
        buildMessageEnvelopeAndPushToOutbox(sagaFailedEvent, saga.getCorrelationId(),
               envelope.getMessageId(),
                saga.getSagaId().toString(),
                "orchestration-events"
                );
    }

    public void publishSagaSucceededEvent(MessageEnvelope<?> envelope, Saga saga) {
        SagaContext sagaContext = this.fetchSagaContext(saga.getSagaId());
        String sagaContextJson = this.sagaContextMapper.toJson(sagaContext);
        SagaSucceededEvent sagaSucceededEvent = new SagaSucceededEvent(
                saga.getSagaId().toString(),
                sagaContextJson,
                sagaContext.getSagaContextType(),
                Instant.now()
        );
        buildMessageEnvelopeAndPushToOutbox(sagaSucceededEvent, saga.getCorrelationId(),
                envelope.getMessageId(),
                saga.getSagaId().toString(),
                "orchestration-events"
        );
    }

    public <T extends AggregateMessage> void buildMessageEnvelopeAndPushToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId,
             String topicName) {
        MessageEnvelope<T> messageEnvelope =
                this.messageEnvelopeFactory
                        .build(correlationId,
                                causationId,
                                sagaId,
                                "SagaService",
                                eventMessage);
        SagaOutbox outboxEvent =
                outboxMapper.mapToOutboxEvent(
                        messageEnvelope,
                        SagaOutbox::new
                );
//        String topicName = forOrchestration ? "orchestration-events"
//                : messageEnvelope.getAggregateType().toLowerCase()+"-commands";
        outboxEvent.setTopicName(topicName);
        this.sagaOutboxRepository.saveAndFlush(outboxEvent);
    }

    public boolean isSagaRetryable(int retryCount){
        return retryCount < retryLimit;
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }

}
