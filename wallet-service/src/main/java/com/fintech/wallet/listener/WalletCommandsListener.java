package com.fintech.wallet.listener;

import com.fintech.common.command.*;
import com.fintech.common.event.*;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.wallet.service.WalletService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class WalletCommandsListener {
    private final MessageDispatcher commandDispatcher;
    private final WalletService walletService;

    public WalletCommandsListener(MessageDispatcher commandDispatcher, WalletService walletService) {
        this.commandDispatcher = commandDispatcher;
        this.walletService = walletService;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
            include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
            //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "wallet-commands", groupId = "wallet-service", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageEnvelope<?> envelope) {
        commandDispatcher.dispatch(envelope);
    }

    /**DltHandler is called when the processing still fails after all retries for all retryable exceptions
     * and also directly handles all the excluded exceptions**/
    @DltHandler
    public void handleDlt(MessageEnvelope<?> envelope, Exception exception){
        String eventType = envelope.getEventType();
        ErrorCode errorCode = mapExceptionToApplicationErrorCode(exception);
        if("CreateWalletCommand".equals(eventType)){
            CreateWalletCommand createWalletCommand = (CreateWalletCommand)envelope.getPayload();
            WalletCreationFailedEvent walletCreationFailedEvent = new WalletCreationFailedEvent(
                    createWalletCommand.walletId().toString(),
                    createWalletCommand.customerId().toString(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletCreationFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("CreditWalletCommand".equals(eventType)){
            CreditWalletCommand creditWalletCommand = (CreditWalletCommand) envelope.getPayload();
            WalletCreditFailedEvent walletCreditFailedEvent = new WalletCreditFailedEvent(
                    creditWalletCommand.accountId().toString(),
                    creditWalletCommand.paymentId().toString(),
                    creditWalletCommand.amount(),
                    creditWalletCommand.currency(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletCreditFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("ReserveWalletFundCommand".equals(eventType)){
            ReserveWalletFundCommand reserveWalletFundCommand = (ReserveWalletFundCommand) envelope.getPayload();
            WalletFundReserveFailedEvent walletFundReserveFailedEvent = new WalletFundReserveFailedEvent(
                    reserveWalletFundCommand.accountId().toString(),
                    reserveWalletFundCommand.paymentId().toString(),
                    reserveWalletFundCommand.amount(),
                    reserveWalletFundCommand.currency(),
                    reserveWalletFundCommand.transactionType(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletFundReserveFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        } else if("CommitWalletFundCommand".equals(eventType)){
            CommitWalletFundCommand commitWalletFundCommand = (CommitWalletFundCommand) envelope.getPayload();
            WalletFundCommitFailedEvent walletFundCommitFailedEvent = new WalletFundCommitFailedEvent(
                    commitWalletFundCommand.accountId().toString(),
                    commitWalletFundCommand.paymentId().toString(),
                    commitWalletFundCommand.amount(),
                    commitWalletFundCommand.currency(),
                    commitWalletFundCommand.transactionType(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletFundCommitFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("TransferWalletAmountCommand".equals(eventType)){
            TransferWalletAmountCommand transferWalletAmountCommand = (TransferWalletAmountCommand) envelope.getPayload();
            WalletFundTransferFailedEvent walletFundTransferFailedEvent = new WalletFundTransferFailedEvent(
                    transferWalletAmountCommand.fromAccount().toString(),
                    transferWalletAmountCommand.toAccount().toString(),
                    transferWalletAmountCommand.paymentId().toString(),
                    transferWalletAmountCommand.amount(),
                    transferWalletAmountCommand.currency(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletFundTransferFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("CompensateWalletFundCommand".equals(eventType)){
            CompensateWalletFundCommand compensateWalletFundCommand = (CompensateWalletFundCommand) envelope.getPayload();
            WalletFundCompensationFailedEvent walletFundCompensationFailedEvent = new WalletFundCompensationFailedEvent(
                    compensateWalletFundCommand.accountId().toString(),
                    compensateWalletFundCommand.paymentId().toString(),
                    compensateWalletFundCommand.amount(),
                    compensateWalletFundCommand.currency(),
                    compensateWalletFundCommand.transactionType(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.walletService.writeFailedEventMessageToOutbox(walletFundCompensationFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }

    }

    private ErrorCode mapExceptionToApplicationErrorCode(Exception exception){
        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        switch (rootCause) {
            case TransientDataAccessException transientDataAccessException -> {
                return CommonErrorCode.DB_QUERY_TIMEOUT;
            }
            case DataAccessResourceFailureException dataAccessResourceFailureException -> {
                return CommonErrorCode.DB_CONNECTION_POOL_TIMEOUT;
            }
            case null, default -> {
                return CommonErrorCode.INTERNAL_ERROR;
            }
        }
    }
}
