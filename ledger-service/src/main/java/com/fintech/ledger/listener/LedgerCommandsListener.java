package com.fintech.ledger.listener;

import com.fintech.common.command.CreateLedgerEntryCommand;
import com.fintech.common.command.CreateWalletLedgerAccountCommand;
import com.fintech.common.domain.LedgerEntryRequest;
import com.fintech.common.event.AccountCreationSagaFailedEvent;
import com.fintech.common.event.LedgerAccountCreationFailedEvent;
import com.fintech.common.event.LedgerEntriesCreationFailedEvent;
import com.fintech.common.event.TransactionSagaFailedEvent;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.ledger.service.LedgerService;
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
import java.util.Optional;
import java.util.UUID;

@Component
public class LedgerCommandsListener {
    private final MessageDispatcher commandDispatcher;
    private final LedgerService ledgerService;

    public LedgerCommandsListener(MessageDispatcher commandDispatcher, LedgerService ledgerService) {
        this.commandDispatcher = commandDispatcher;
        this.ledgerService = ledgerService;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
            include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
            //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "ledger-commands", groupId = "ledger-service", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageEnvelope<?> envelope) {
        commandDispatcher.dispatch(envelope);
    }

    /**DltHandler is called when the processing still fails after all retries for all retryable exceptions
     * and also directly handles all the excluded exceptions, if not already handled**/
    @DltHandler
    public void handleDlt(MessageEnvelope<?> envelope, Exception exception){
        String eventType = envelope.getEventType();
        ErrorCode errorCode = mapExceptionToApplicationErrorCode(exception);
        if("CreateWalletLedgerAccountCommand".equals(eventType)){
            CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand = (CreateWalletLedgerAccountCommand)envelope.getPayload();
            LedgerAccountCreationFailedEvent ledgerAccountCreationFailedEvent = new LedgerAccountCreationFailedEvent(
                    createWalletLedgerAccountCommand.ledgerAccountId().toString(),
                    createWalletLedgerAccountCommand.customerId().toString(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.ledgerService.writeFailedEventMessageToOutbox(ledgerAccountCreationFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("CreateLedgerEntryCommand".equals(eventType)){
            CreateLedgerEntryCommand createLedgerEntryCommand = (CreateLedgerEntryCommand)envelope.getPayload();
            LedgerEntriesCreationFailedEvent ledgerEntriesCreationFailedEvent = new LedgerEntriesCreationFailedEvent(
                    createLedgerEntryCommand.sourceAcountId(),
                    createLedgerEntryCommand.destinationAccountId(),
                    createLedgerEntryCommand.paymentId(),
                    createLedgerEntryCommand.transactionType(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.ledgerService.writeFailedEventMessageToOutbox(ledgerEntriesCreationFailedEvent, envelope.getCorrelationId(),
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
