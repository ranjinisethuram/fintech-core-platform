package com.fintech.wallet.service;

import com.fintech.common.command.*;
import com.fintech.common.event.*;
import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.outbox.OutboxMapper;
import com.fintech.wallet.domain.FundReservation;
import com.fintech.wallet.domain.FundReservationStatus;
import com.fintech.wallet.domain.Wallet;
import com.fintech.wallet.domain.WalletStatus;
import com.fintech.wallet.dto.WalletBalance;
import com.fintech.wallet.exception.WalletErrorCode;
import com.fintech.wallet.outbox.WalletOutboxEvent;
import com.fintech.wallet.outbox.WalletOutboxRepository;
import com.fintech.wallet.repository.FundReservationRepository;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletOutboxRepository walletOutboxRepository;
    private final FundReservationRepository fundReservationRepository;
    private final OutboxMapper outboxMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;

    public WalletService(WalletRepository walletRepository, WalletOutboxRepository walletOutboxRepository, FundReservationRepository fundReservationRepository, OutboxMapper outboxMapper, MessageEnvelopeFactory messageEnvelopeFactory) {
        this.walletRepository = walletRepository;
        this.walletOutboxRepository = walletOutboxRepository;
        this.fundReservationRepository = fundReservationRepository;
        this.outboxMapper = outboxMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
    }

    @Transactional
    public void createWallet(CreateWalletCommand createWalletCommand, String correlationId,
                             String causationId, String sagaId){
        try{
            Wallet wallet = this.walletRepository.saveAndFlush(new Wallet(createWalletCommand.walletId()
                    , createWalletCommand.accountId()));
            WalletCreatedEvent walletCreatedEvent = new WalletCreatedEvent(wallet.getWalletId().toString(),
                    wallet.getAccountId().toString(), createWalletCommand.customerId().toString()
                    , wallet.getCurrency(), Instant.now());

            buildMessageEnvelopeAndPushToOutbox(walletCreatedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            WalletCreationFailedEvent walletCreationFailedEvent = new WalletCreationFailedEvent(
                    createWalletCommand.customerId().toString(),
                    sagaId,
                    WalletErrorCode.WALLET_ALREADY_EXISTS.getErrorCode(),
                    WalletErrorCode.WALLET_ALREADY_EXISTS.getErrorMessage(),
                    WalletErrorCode.WALLET_ALREADY_EXISTS.isRetryable(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(walletCreationFailedEvent,correlationId,causationId,sagaId);
        }
    }

    @Transactional
    public void creditWallet(CreditWalletCommand creditWalletCommand, String correlationId,
                             String causationId, String sagaId) {
        try {
            Wallet updatedWallet = creditAmount(creditWalletCommand.accountId(), creditWalletCommand.amount());
            WalletCreditedEvent walletCreditedEvent = new WalletCreditedEvent(
                    updatedWallet.getAccountId().toString(),
                    creditWalletCommand.paymentId().toString(),
                    creditWalletCommand.amount(),
                    updatedWallet.getAvailableBalance(),
                    updatedWallet.getCurrency(),
                    creditWalletCommand.transactionType(),
                    updatedWallet.getUpdatedAt()
            );
            buildMessageEnvelopeAndPushToOutbox(walletCreditedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch(BaseException exception) {
            WalletErrorCode walletErrorCode = (WalletErrorCode)exception.getErrorCode();
            buildWalletCreditFailedEventMessage(creditWalletCommand,
                    walletErrorCode,
                    correlationId,
                    causationId,
                    sagaId);
        }
    }

    @Transactional
    public void reserveFunds(ReserveWalletFundCommand reserveWalletFundCommand,
                            String correlationId, String causationId, String sagaId) {

        Wallet wallet = this.fetchWalletByAccountId(reserveWalletFundCommand.accountId());
        if(wallet != null && wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            //wallet.lockAmount(reserveWalletFundCommand.amount());
            int rowsUpdated = this.walletRepository.reserveFunds(wallet.getWalletId(),
                    reserveWalletFundCommand.amount(), Instant.now());
            if(rowsUpdated == 0){
                buildFundReservationFailedEventMessage(reserveWalletFundCommand,
                        WalletErrorCode.WALLET_INSUFFICIENT_BALANCE,
                        correlationId,
                        causationId,
                        sagaId
                );
            }else{
                persistFundReservation(wallet, reserveWalletFundCommand,
                        correlationId, causationId, sagaId);
            }

        }else if(wallet == null){
            //wallet not found
            buildFundReservationFailedEventMessage(reserveWalletFundCommand,
                    WalletErrorCode.WALLET_NOT_EXISTS,
                    correlationId,
                    causationId,
                    sagaId
            );
        }else if(!wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            //wallet not active
            buildFundReservationFailedEventMessage(reserveWalletFundCommand,
                    WalletErrorCode.WALLET_NOT_ACTIVE,
                    correlationId,
                    causationId,
                    sagaId);
        }

    }

    @Transactional
    public void transferWalletFunds(TransferWalletAmountCommand transferWalletAmountCommand,
            String correlationId, String causationId, String sagaId){
        try{
            releaseLockedAmount(transferWalletAmountCommand.fromAccount(),
                    transferWalletAmountCommand.paymentId(),
                    transferWalletAmountCommand.amount());
            creditAmount(transferWalletAmountCommand.toAccount(),
                    transferWalletAmountCommand.amount());
            WalletFundTransferredEvent walletFundTransferredEvent = new WalletFundTransferredEvent(
                    transferWalletAmountCommand.fromAccount().toString(),
                    transferWalletAmountCommand.toAccount().toString(),
                    transferWalletAmountCommand.paymentId().toString(),
                    transferWalletAmountCommand.amount(),
                    transferWalletAmountCommand.currency(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(walletFundTransferredEvent,
                    correlationId, causationId, sagaId);
        }catch (BaseException exception) {
            WalletErrorCode walletErrorCode = (WalletErrorCode)exception.getErrorCode();
            buildFundTransferFailedEventMessage(transferWalletAmountCommand,
                    walletErrorCode,
                    correlationId,
                    causationId,
                    sagaId
            );
        }
    }

    @Transactional
    public void commitFunds(CommitWalletFundCommand commitWalletFundCommand,
                            String correlationId, String causationId, String sagaId) {
        try {
            Wallet wallet = releaseLockedAmount(commitWalletFundCommand.accountId(),
                    commitWalletFundCommand.paymentId(),
                    commitWalletFundCommand.amount());
            WalletFundCommittedEvent walletFundCommittedEvent = new WalletFundCommittedEvent(
                    wallet.getAccountId().toString(),
                    commitWalletFundCommand.paymentId().toString(),
                    commitWalletFundCommand.amount(),
                    wallet.getAvailableBalance(),
                    wallet.getCurrency(),
                    commitWalletFundCommand.transactionType(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(walletFundCommittedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch (BaseException exception) {
            WalletErrorCode walletErrorCode = (WalletErrorCode)exception.getErrorCode();
            buildFundCommitFailedEventMessage(commitWalletFundCommand,
                    walletErrorCode,
                    correlationId,
                    causationId,
                    sagaId
            );
        }
    }

    public void compensateFunds(CompensateWalletFundCommand compensateWalletFundCommand,
                 String correlationId, String causationId, String sagaId){
        Wallet wallet = this.fetchWalletByAccountId(compensateWalletFundCommand.accountId());
        if(wallet != null && wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            compensateFunds(wallet, compensateWalletFundCommand,
                    correlationId, causationId, sagaId);
            WalletFundCompensatedEvent walletFundCompensatedEvent = new
                    WalletFundCompensatedEvent(
                    compensateWalletFundCommand.accountId().toString(),
                    compensateWalletFundCommand.paymentId().toString(),
                    compensateWalletFundCommand.amount(),
                    compensateWalletFundCommand.currency(),
                    compensateWalletFundCommand.transactionType(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(walletFundCompensatedEvent,
                    correlationId, causationId, sagaId);

        }else if(wallet == null){
            //wallet not found
            buildFundCompensationFailedEventMessage(compensateWalletFundCommand,
                    WalletErrorCode.WALLET_NOT_EXISTS,
                    correlationId,
                    causationId,
                    sagaId
            );
        }else if(!wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            //wallet not active
            buildFundCompensationFailedEventMessage(compensateWalletFundCommand,
                    WalletErrorCode.WALLET_NOT_ACTIVE,
                    correlationId,
                    causationId,
                    sagaId);
        }
    }

    public WalletAccountValidationResponse validateWalletAccount(WalletAccountValidationRequest walletAccountValidationRequest){
        WalletAccountValidationResponse walletAccountValidationResponse =
                new WalletAccountValidationResponse();
        Wallet wallet = this.fetchWalletByAccountId(walletAccountValidationRequest.getAccountId());
        if(wallet != null && !wallet.getWalletStatus().name().equals("ACTIVE")){
            //throw Wallet Not Active
            throw new BaseException(WalletErrorCode.WALLET_NOT_ACTIVE);
        } else if(wallet == null){
            //throw wallet does not exist
            throw new BaseException(WalletErrorCode.WALLET_NOT_EXISTS);
        }
        walletAccountValidationResponse.setStatus(true);
        return walletAccountValidationResponse;
    }

    public Wallet fetchWalletByAccountId(UUID accountId) {
        return this.walletRepository.findByAccountId(accountId);
    }

    public FundReservation fetchFundReservation(UUID walletId, UUID transactionId){
        return this.fundReservationRepository.findByWalletIdAndTransactionId(walletId, transactionId);
    }

    public WalletBalance getAvailableBalance(UUID accountId){
        Wallet wallet = this.walletRepository.findByAccountId(accountId);
        WalletBalance walletBalance = new WalletBalance();
        walletBalance.setAccountId(wallet.getAccountId());
        walletBalance.setAvailableBalance(wallet.getAvailableBalance());
        walletBalance.setCurrency(wallet.getCurrency());
        return walletBalance;
    }

    private Wallet creditAmount(UUID accountId, BigDecimal amount) throws BaseException {
        Wallet wallet = this.fetchWalletByAccountId(accountId);
        if(wallet != null && wallet.getWalletStatus().equals(WalletStatus.ACTIVE)) {
            wallet.creditAmount(amount);
            wallet = this.walletRepository.save(wallet);
        }else if(wallet == null){
            throw new BaseException(WalletErrorCode.WALLET_NOT_EXISTS);
        }else if(!wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            throw new BaseException(WalletErrorCode.WALLET_NOT_ACTIVE);
        }
        return  wallet;
    }

    private Wallet releaseLockedAmount(UUID accountId, UUID transactionId, BigDecimal amount)
            throws BaseException{
        Wallet wallet = this.fetchWalletByAccountId(accountId);
        if(wallet != null && wallet.getWalletStatus().equals(WalletStatus.ACTIVE)) {
            wallet.releaseLockedAmount(amount);
            wallet = this.walletRepository.save(wallet);
            FundReservation fundReservation = fetchFundReservation(wallet.getWalletId(),
                    transactionId);
            if(fundReservation == null){
                throw new BaseException(WalletErrorCode.WALLET_FUND_RESERVATION_NOT_FOUND);
            }else{
                fundReservation.changeFundReservationStatus(FundReservationStatus.CONSUMED);
                this.fundReservationRepository.save(fundReservation);
            }
        }else if(wallet == null){
            throw new BaseException(WalletErrorCode.WALLET_NOT_EXISTS);
        }else if(!wallet.getWalletStatus().equals(WalletStatus.ACTIVE)){
            throw new BaseException(WalletErrorCode.WALLET_NOT_ACTIVE);
        }
        return wallet;
    }

    private void compensateFunds(Wallet wallet, CompensateWalletFundCommand compensateWalletFundCommand,
                                 String correlationId,
                                 String causationId,
                                 String sagaId){
        wallet.compensateAvailableBalance(compensateWalletFundCommand.amount());
        this.walletRepository.save(wallet);
        FundReservation fundReservation = fetchFundReservation(wallet.getWalletId(), compensateWalletFundCommand.paymentId());
        if(fundReservation == null){
            buildFundCompensationFailedEventMessage(compensateWalletFundCommand,
                    WalletErrorCode.WALLET_FUND_RESERVATION_NOT_FOUND,
                    correlationId,
                    causationId,
                    sagaId
            );
        }else{
            fundReservation.changeFundReservationStatus(FundReservationStatus.COMPENSATED);
            this.fundReservationRepository.save(fundReservation);
        }
    }

    private void buildWalletCreditFailedEventMessage(CreditWalletCommand creditWalletCommand,
                     WalletErrorCode walletErrorCode, String correlationId,
                     String causationId, String sagaId){
        WalletCreditFailedEvent walletCreditFailedEvent = new WalletCreditFailedEvent(
                creditWalletCommand.accountId().toString(),
                creditWalletCommand.paymentId().toString(),
                creditWalletCommand.amount(),
                creditWalletCommand.currency(),
                walletErrorCode.getErrorCode(),
                walletErrorCode.getErrorMessage(),
                walletErrorCode.isRetryable(),
                Instant.now()
        );
        buildMessageEnvelopeAndPushToOutbox(walletCreditFailedEvent,
                correlationId,
                causationId,
                sagaId);
    }

    private void buildFundReservationFailedEventMessage(ReserveWalletFundCommand reserveWalletFundCommand,
                    WalletErrorCode walletErrorCode, String correlationId,
                    String causationId, String sagaId){
        WalletFundReserveFailedEvent walletFundReserveFailedEvent = new WalletFundReserveFailedEvent(
                reserveWalletFundCommand.accountId().toString(),
                reserveWalletFundCommand.paymentId().toString(),
                reserveWalletFundCommand.amount(),
                reserveWalletFundCommand.currency(),
                reserveWalletFundCommand.transactionType(),
                walletErrorCode.getErrorCode(),
                walletErrorCode.getErrorMessage(),
                walletErrorCode.isRetryable(),
                Instant.now());
        buildMessageEnvelopeAndPushToOutbox(walletFundReserveFailedEvent,
                correlationId,
                causationId,
                sagaId);
    }

    private void buildFundCommitFailedEventMessage(CommitWalletFundCommand commitWalletFundCommand,
                    WalletErrorCode walletErrorCode, String correlationId,
                    String causationId, String sagaId){
        WalletFundCommitFailedEvent walletFundCommitFailedEvent= new WalletFundCommitFailedEvent(
                commitWalletFundCommand.accountId().toString(),
                commitWalletFundCommand.paymentId().toString(),
                commitWalletFundCommand.amount(),
                commitWalletFundCommand.currency(),
                commitWalletFundCommand.transactionType(),
                walletErrorCode.getErrorCode(),
                walletErrorCode.getErrorMessage(),
                walletErrorCode.isRetryable(),
                Instant.now());
        buildMessageEnvelopeAndPushToOutbox(walletFundCommitFailedEvent,
                correlationId,
                causationId,
                sagaId);
    }

    private void buildFundTransferFailedEventMessage(TransferWalletAmountCommand transferWalletAmountCommand,
         WalletErrorCode walletErrorCode, String correlationId,
         String causationId, String sagaId ){
        WalletFundTransferFailedEvent walletFundTransferFailedEvent = new
            WalletFundTransferFailedEvent(
                transferWalletAmountCommand.fromAccount().toString(),
                transferWalletAmountCommand.toAccount().toString(),
                transferWalletAmountCommand.paymentId().toString(),
                transferWalletAmountCommand.amount(),
                transferWalletAmountCommand.currency(),
                walletErrorCode.getErrorCode(),
                walletErrorCode.getErrorMessage(),
                walletErrorCode.isRetryable(),
                Instant.now());
        buildMessageEnvelopeAndPushToOutbox(walletFundTransferFailedEvent,
                correlationId, causationId, sagaId);
    }

    private void buildFundCompensationFailedEventMessage(CompensateWalletFundCommand compensateWalletFundCommand,
               WalletErrorCode walletErrorCode, String correlationId,
               String causationId, String sagaId){
        WalletFundCompensationFailedEvent walletFundCompensationFailedEvent= new
                WalletFundCompensationFailedEvent(
                compensateWalletFundCommand.accountId().toString(),
                compensateWalletFundCommand.paymentId().toString(),
                compensateWalletFundCommand.amount(),
                compensateWalletFundCommand.currency(),
                compensateWalletFundCommand.transactionType(),
                walletErrorCode.getErrorCode(),
                walletErrorCode.getErrorMessage(),
                walletErrorCode.isRetryable(),
                Instant.now());
        buildMessageEnvelopeAndPushToOutbox(walletFundCompensationFailedEvent,
                correlationId,
                causationId,
                sagaId);
    }

    private void persistFundReservation(Wallet wallet,
                        ReserveWalletFundCommand reserveWalletFundCommand,
                        String correlationId, String causationId, String sagaId){
        try {
            FundReservation fundReservation = new FundReservation(wallet.getWalletId(),
                    reserveWalletFundCommand.paymentId(),
                    reserveWalletFundCommand.amount(),
                    Instant.now().plusSeconds(120));
            FundReservation fundsReserved = this.fundReservationRepository.saveAndFlush(fundReservation);
            WalletFundReservedEvent walletFundReservedEvent = new WalletFundReservedEvent(
                    wallet.getAccountId().toString(),
                    reserveWalletFundCommand.paymentId().toString(),
                    reserveWalletFundCommand.amount(),
                    reserveWalletFundCommand.currency(),
                    reserveWalletFundCommand.transactionType(),
                    fundsReserved.getUpdatedAt()
            );
            buildMessageEnvelopeAndPushToOutbox(walletFundReservedEvent,
                    correlationId,
                    causationId,
                    sagaId);
        }catch(DataIntegrityViolationException ex){
            buildFundReservationFailedEventMessage(reserveWalletFundCommand,
                    WalletErrorCode.WALLET_DUPLICATE_FUND_RESERVATION,
                    correlationId,
                    causationId,
                    sagaId
            );
        }
    }

    private <T extends AggregateMessage> void buildMessageEnvelopeAndPushToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        MessageEnvelope<T> messageEnvelope =
                this.messageEnvelopeFactory
                        .build(correlationId,
                                causationId,
                                sagaId,
                                "WalletService",
                                eventMessage);
        WalletOutboxEvent walletOutboxEvent = outboxMapper.mapToOutboxEvent(messageEnvelope,
                WalletOutboxEvent::new);
        this.walletOutboxRepository.saveAndFlush(walletOutboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends AggregateMessage> void writeFailedEventMessageToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        this.buildMessageEnvelopeAndPushToOutbox(eventMessage,correlationId,causationId,sagaId);
    }
}
