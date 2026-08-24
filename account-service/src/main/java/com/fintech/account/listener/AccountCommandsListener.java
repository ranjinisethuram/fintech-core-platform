package com.fintech.account.listener;

import com.fintech.account.service.AccountService;
import com.fintech.common.command.ActivateAccountCommand;
import com.fintech.common.command.CreateAccountCommand;
import com.fintech.common.event.AccountActivationFailedEvent;
import com.fintech.common.event.AccountCreationSagaFailedEvent;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
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
public class AccountCommandsListener {

    private final MessageDispatcher commandDispatcher;
    private final AccountService accountService;

    public AccountCommandsListener(MessageDispatcher commandDispatcher, AccountService accountService) {
        this.commandDispatcher = commandDispatcher;
        this.accountService = accountService;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
    include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
    //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
    dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "account-commands", groupId = "account-service", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageEnvelope<?> envelope) {
        commandDispatcher.dispatch(envelope);
    }

    /**DltHandler is called when the processing still fails after all retries for all retryable exceptions
     * and also directly handles all the excluded exceptions, if not already handled**/
    @DltHandler
    public void handleDlt(MessageEnvelope<?> envelope, Exception exception){
        String eventType = envelope.getEventType();
        ErrorCode errorCode = mapExceptionToApplicationErrorCode(exception);

        if("CreateAccountCommand".equals(eventType)){
            CreateAccountCommand createAccountCommand = (CreateAccountCommand)envelope.getPayload();
            AccountCreationSagaFailedEvent accountCreationSagaFailedEvent = new
                    AccountCreationSagaFailedEvent(
                    createAccountCommand.accountId().toString(),
                    createAccountCommand.customerId().toString(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.accountService.writeFailedEventMessageToOutbox(accountCreationSagaFailedEvent, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }else if("ActivateAccountCommand".equals(eventType)){
            ActivateAccountCommand activateAccountCommand = (ActivateAccountCommand) envelope.getPayload();
            AccountActivationFailedEvent accountActivationFailedEvent = new
                    AccountActivationFailedEvent(
                    activateAccountCommand.accountId().toString(),
                    activateAccountCommand.customerId().toString(),
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    errorCode.isRetryable(),
                    Instant.now()
            );
            this.accountService.writeFailedEventMessageToOutbox(accountActivationFailedEvent, envelope.getCorrelationId(),
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
                //log the actual exception
                return CommonErrorCode.INTERNAL_ERROR;
            }
        }
    }

}
