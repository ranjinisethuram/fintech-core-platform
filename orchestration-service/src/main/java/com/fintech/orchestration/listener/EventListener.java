package com.fintech.orchestration.listener;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
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

@Component
public class EventListener {

    private final MessageDispatcher eventDispatcher;
    private final SagaService sagaService;

    public EventListener(MessageDispatcher eventDispatcher, SagaService sagaService) {
        this.eventDispatcher = eventDispatcher;
        this.sagaService = sagaService;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
            include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
            //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = {"customer-events","account-events","wallet-events","ledger-events"
            ,"transaction-events"},
            groupId = "orchestrator-service", containerFactory = "kafkaListenerContainerFactory")
    public void handle(MessageEnvelope<?> messageEnvelope){
        eventDispatcher.dispatch(messageEnvelope);
    }

    /**DltHandler is called when the processing still fails after all retries for all retryable exceptions
     * and also directly handles all the excluded exceptions**/
    @DltHandler
    public void handleDlt(MessageEnvelope<?> envelope, Exception exception){
        try {
            ErrorCode errorCode = mapExceptionToApplicationErrorCode(exception);
            //emit saga failed event to kafka
            this.sagaService.handleSagaProcessingException(envelope,errorCode);
        } catch (Exception e) {
            //log the exception and exit to safely commit the kafka offset and to prevent infinite retries.
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
            case DeserializationException deserializationException -> {
                return OrchestrationErrorCode.ORCH_EVENT_DESERIALIZATION_FAILED;
            }
            case BaseException baseException -> {
                return baseException.getErrorCode();
            }
            case null, default -> {
                return CommonErrorCode.INTERNAL_ERROR;
            }
        }
    }
}
