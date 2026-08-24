package com.fintech.ledger.service;

import com.fintech.common.command.CreateLedgerEntryCommand;
import com.fintech.common.command.CreateWalletLedgerAccountCommand;
import com.fintech.common.domain.LedgerEntryRequest;
import com.fintech.common.domain.LedgerEntryType;
import com.fintech.common.domain.TransactionType;
import com.fintech.common.event.*;
import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.ledger.domain.AccountCode;
import com.fintech.ledger.domain.Ledger;
import com.fintech.ledger.domain.LedgerAccount;
import com.fintech.ledger.exception.LedgerErrorCode;
import com.fintech.ledger.outbox.LedgerOutboxEvent;
import com.fintech.ledger.outbox.LedgerOutboxPoller;
import com.fintech.ledger.outbox.LedgerOutboxRepository;
import com.fintech.ledger.repository.LedgerAccountRepository;
import com.fintech.ledger.repository.LedgerRepository;
import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;
import com.fintech.outbox.OutboxMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerOutboxRepository ledgerOutboxRepository;
    private final OutboxMapper outboxMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;

    public LedgerService(LedgerRepository ledgerRepository
            , LedgerAccountRepository ledgerAccountRepository
            , LedgerOutboxRepository ledgerOutboxRepository, OutboxMapper outboxMapper, MessageEnvelopeFactory messageEnvelopeFactory) {
        this.ledgerRepository = ledgerRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerOutboxRepository = ledgerOutboxRepository;
        this.outboxMapper = outboxMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
    }

    @Transactional
    public void createLedgerEntries(CreateLedgerEntryCommand createLedgerEntryCommand, String correlationId,
                                  String causationId, String sagaId){
        Map<String,UUID> sourceDestinationAccountIds = resolveSourceDestinationAccountIds(createLedgerEntryCommand);
        try {
            List<Ledger> ledgerEntries = new ArrayList<>();
            ledgerEntries.add(new Ledger(createLedgerEntryCommand.paymentId()
                    , sourceDestinationAccountIds.get("sourceAccountId")
                    , createLedgerEntryCommand.amount()
                    , createLedgerEntryCommand.currency()
                    , false));

            ledgerEntries.add(new Ledger(createLedgerEntryCommand.paymentId()
                    , sourceDestinationAccountIds.get("destinationAccountId")
                    , createLedgerEntryCommand.amount()
                    , createLedgerEntryCommand.currency()
                    , true));
            this.ledgerRepository.saveAllAndFlush(ledgerEntries);

            LedgerEntriesCreatedEvent ledgerEntryCreatedEvent = new LedgerEntriesCreatedEvent(
                    createLedgerEntryCommand.paymentId().toString(),
                    sourceDestinationAccountIds.get("sourceAccountId"),
                    sourceDestinationAccountIds.get("destinationAccountId"),
                    createLedgerEntryCommand.amount(),
                    createLedgerEntryCommand.currency(),
                    createLedgerEntryCommand.transactionType(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(ledgerEntryCreatedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            LedgerEntriesCreationFailedEvent ledgerEntriesCreationFailedEvent = new LedgerEntriesCreationFailedEvent(
                    createLedgerEntryCommand.sourceAcountId(),
                    createLedgerEntryCommand.destinationAccountId(),
                    createLedgerEntryCommand.paymentId(),
                    createLedgerEntryCommand.transactionType(),
                    LedgerErrorCode.LEDGER_ENTRY_ALREADY_EXISTS.getErrorCode(),
                    LedgerErrorCode.LEDGER_ENTRY_ALREADY_EXISTS.getErrorMessage(),
                    LedgerErrorCode.LEDGER_ENTRY_ALREADY_EXISTS.isRetryable(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(ledgerEntriesCreationFailedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }
    }

    @Transactional
    public void createCustomerLedgerAccount(CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand,
                                            String correlationId, String causationId, String sagaId){
        try{
            LedgerAccount ledgerAccount = new LedgerAccount(createWalletLedgerAccountCommand.ledgerAccountId(),
                    AccountCode.USER_WALLET, createWalletLedgerAccountCommand.currency(),
                    createWalletLedgerAccountCommand.walletId());
            this.ledgerAccountRepository.saveAndFlush(ledgerAccount);
            LedgerAccountCreatedEvent ledgerAccountCreatedEvent = new LedgerAccountCreatedEvent(
                    ledgerAccount.getId().toString(),
                    createWalletLedgerAccountCommand.accountId().toString(),
                    createWalletLedgerAccountCommand.customerId().toString(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(ledgerAccountCreatedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            LedgerAccountCreationFailedEvent ledgerAccountCreationFailedEvent = new LedgerAccountCreationFailedEvent(
                    createWalletLedgerAccountCommand.ledgerAccountId().toString(),
                    createWalletLedgerAccountCommand.customerId().toString(),
                    LedgerErrorCode.LEDGER_ACCOUNT_ALREADY_EXISTS.getErrorCode(),
                    LedgerErrorCode.LEDGER_ACCOUNT_ALREADY_EXISTS.getErrorMessage(),
                    LedgerErrorCode.LEDGER_ACCOUNT_ALREADY_EXISTS.isRetryable(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(ledgerAccountCreationFailedEvent,correlationId,causationId,sagaId);
        }
    }

    public LedgerAccountValidationResponse validateLedgerAccount(
            LedgerAccountValidationRequest ledgerAccountValidationRequest){
        LedgerAccountValidationResponse ledgerAccountValidationResponse =
                new LedgerAccountValidationResponse();
        LedgerAccount ledgerAccount = this.ledgerAccountRepository
                .findById(ledgerAccountValidationRequest.getLedgerAccountId()).orElse(null);
        if(ledgerAccount != null){
            if(!ledgerAccount.getWalletId().equals(ledgerAccountValidationRequest.getWalletId())){
                throw new BaseException(LedgerErrorCode.LEDGER_ACCOUNT_UNAUTHORIZED);
            }
        }else{
            throw new BaseException(LedgerErrorCode.LEDGER_ACCOUNT_NOT_EXISTS);
        }
        return ledgerAccountValidationResponse;
    }

    public UUID fetchIdByAccountCode(AccountCode accountCode){
        return this.ledgerAccountRepository.findByAccountCode(accountCode).getId();
    }

    private Map<String,UUID> resolveSourceDestinationAccountIds(CreateLedgerEntryCommand
                            createLedgerEntryCommand){
        Map<String,UUID> accountIds = new HashMap<>();

        switch(createLedgerEntryCommand.transactionType()){
            case DEPOSIT -> {
//                LedgerEntryRequest ledgerEntryRequest = createLedgerEntryCommand.ledgerEntryRequests().stream()
//                        .filter(request -> request.getLedgerEntryType()
//                                .equals(LedgerEntryType.CREDIT)).findFirst().orElse(null);
                UUID destinationAccountId = createLedgerEntryCommand.destinationAccountId();
                UUID sourceAccountId =  fetchIdByAccountCode(AccountCode.PLATFORM_BANK);
                accountIds.put("sourceAccountId",sourceAccountId);
                accountIds.put("destinationAccountId",destinationAccountId);

            }
            case WITHDRAWAL -> {
//                LedgerEntryRequest ledgerEntryRequest = createLedgerEntryCommand.ledgerEntryRequests().stream()
//                        .filter(request -> request.getLedgerEntryType()
//                                .equals(LedgerEntryType.DEBIT)).findFirst().orElse(null);
                UUID sourceAccountId = createLedgerEntryCommand.sourceAcountId();
                UUID destinationAccountId = fetchIdByAccountCode(AccountCode.PLATFORM_BANK);
                accountIds.put("sourceAccountId",sourceAccountId);
                accountIds.put("destinationAccountId",destinationAccountId);
            }
            case TRANSFER -> {
//                Map<LedgerEntryType, List<LedgerEntryRequest>> sourceDestinationLedgerEntries = createLedgerEntryCommand.ledgerEntryRequests().stream()
//                        .collect(Collectors.groupingBy(LedgerEntryRequest::getLedgerEntryType));
//                LedgerEntryRequest sourceLedgerEntryRequest = sourceDestinationLedgerEntries
//                        .get(LedgerEntryType.DEBIT).stream()
//                        .findFirst().orElse(null);
                UUID sourceAccountId = createLedgerEntryCommand.sourceAcountId();
//                LedgerEntryRequest destinationLedgerEntryRequest = sourceDestinationLedgerEntries
//                        .get(LedgerEntryType.CREDIT).stream()
//                        .findFirst().orElse(null);
                UUID destinationAccountId = createLedgerEntryCommand.destinationAccountId();
                accountIds.put("sourceAccountId",sourceAccountId);
                accountIds.put("destinationAccountId",destinationAccountId);
            }
        }
        return accountIds;
    }

    private <T extends AggregateMessage> void buildMessageEnvelopeAndPushToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        MessageEnvelope<T> messageEnvelope =
                this.messageEnvelopeFactory
                        .build(correlationId,
                                causationId,
                                sagaId,
                                "LedgerService",
                                eventMessage);
        LedgerOutboxEvent ledgerOutboxEvent = outboxMapper.mapToOutboxEvent(messageEnvelope,
                LedgerOutboxEvent::new);
        this.ledgerOutboxRepository.saveAndFlush(ledgerOutboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends AggregateMessage> void writeFailedEventMessageToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        this.buildMessageEnvelopeAndPushToOutbox(eventMessage,correlationId,causationId,sagaId);
    }
}
