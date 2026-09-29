package com.fintech.orchestration.listener;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Component
public class EventListener {

    private final MessageDispatcher eventDispatcher;
    private final SagaService sagaService;
    private final JsonMapper jsonMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;

    public EventListener(MessageDispatcher eventDispatcher, SagaService sagaService, JsonMapper jsonMapper, MessageEnvelopeFactory messageEnvelopeFactory) {
        this.eventDispatcher = eventDispatcher;
        this.sagaService = sagaService;
        this.jsonMapper = jsonMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
            include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
            //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = {"customer-events","account-events","wallet-events","ledger-events"
            ,"transaction-events"},
            groupId = "orchestration-service",
            containerFactory = "dynamicEventContainerFactory")
    public void handle(ConsumerRecord<?, ?> record){
        try {
//            MessageEnvelope<?> messageEnvelope = (MessageEnvelope<?>) message.getPayload();

//            System.out.println("Processing event type: " + messageEnvelope.getEventType());
            String rawPayload;

            // Check if the value is a byte array and convert it to String safely
            if (record.value() instanceof byte[] bytes) {
                rawPayload = new String(bytes, StandardCharsets.UTF_8);
            } else if (record.value() instanceof String str) {
                rawPayload = str;
            } else {
                rawPayload = String.valueOf(record.value());
            }

            System.out.println("Received message {}: " + rawPayload);
            MessageEnvelope<?> messageEnvelope = jsonMapper.readValue(rawPayload, new TypeReference<MessageEnvelope<?>>() {});
            eventDispatcher.dispatch(messageEnvelope);
        }catch (Exception businessException) {
            System.err.println("=================================================");
            System.err.println("🔥 CRITICAL BUSINESS LOGIC ERROR IN SAGA HANDLER:");
            businessException.printStackTrace(); // This is the real error causing the saga to stall!
            System.err.println("=================================================");
            throw businessException; // Let it bubble up safely
        }
    }

    /**DltHandler is called when the processing still fails after all retries for all retryable exceptions
     * and also directly handles all the excluded exceptions**/
    @DltHandler
    public void handleDlt(ConsumerRecord<?, ?> record,
                          @Header(KafkaHeaders.EXCEPTION_FQCN) Object exceptionClassName,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) Object exceptionMessage,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){

        String rawPayload;

        // Check if the value is a byte array and convert it to String safely
        if (record.value() instanceof byte[] bytes) {
            rawPayload = new String(bytes, StandardCharsets.UTF_8);
        } else if (record.value() instanceof String str) {
            rawPayload = str;
        } else {
            rawPayload = String.valueOf(record.value());
        }

        String className = convertHeaderToString(exceptionClassName);
        String errMsg = convertHeaderToString(exceptionMessage);

        System.err.println("DLT HANDLER INVOKED FOR TOPIC: " + topic +
                " | Exception: " + className + " | Message: " + errMsg);
        if (className.contains("DeserializationException") ||
                className.contains("MismatchedInputException")) {
            System.err.println("CRITICAL: Corrupt JSON format. Cannot map to Record.");
            System.err.println("Raw Poison Pill Payload: " + rawPayload);
        }else {
            try{
                ErrorCode errorCode = mapExceptionToApplicationErrorCode(exception);
                MessageEnvelope<?> envelope = jsonMapper.readValue(rawPayload, MessageEnvelope.class);
                System.err.println("DLT HANDLER: Emitting saga failed event to kafka for envelope: " + envelope.toString() +
                        " | Error Code: " + errorCode.getErrorCode() + " | Error Message: " + errorCode.getErrorMessage());
                //emit saga failed event to kafka
                this.sagaService.handleSagaProcessingException(envelope,errorCode);
            } catch (Exception e) {
                System.err.println("DLT HANDLER ERROR: Failed to deserialize raw payload or handle saga exception. Raw Payload: " + rawPayload);
                System.err.println("Exception: " + exceptionClassName + " | Message: " + errMsg);
                //log the exception and exit to safely commit the kafka offset and to prevent infinite retries.
            }

        }
    }

    private ErrorCode mapExceptionToApplicationErrorCode(String exceptionClassName){
//        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        switch (exceptionClassName) {
            case "TransientDataAccessException" -> {
                return CommonErrorCode.DB_QUERY_TIMEOUT;
            }
            case "DataAccessResourceFailureException" -> {
                return CommonErrorCode.DB_CONNECTION_POOL_TIMEOUT;
            }
            case "DeserializationException" -> {
                return OrchestrationErrorCode.ORCH_EVENT_DESERIALIZATION_FAILED;
            }
            case "BaseException" -> {
                return CommonErrorCode.INTERNAL_ERROR;
            }
            case null, default -> {
                return CommonErrorCode.INTERNAL_ERROR;
            }
        }
    }
}
