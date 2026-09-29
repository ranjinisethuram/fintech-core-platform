package com.fintech.transaction.listener;

import com.fintech.common.event.OrchestrationErrorEvent;
import com.fintech.common.event.SagaFailedEvent;
import com.fintech.common.event.SagaSucceededEvent;
import com.fintech.common.event.TransactionSagaSucceededEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.transaction.repository.ProcessedMessagesRepository;
import com.fintech.transaction.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventListener {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final TransactionService transactionService;

    public EventListener(ProcessedMessagesRepository processedMessagesRepository,
                         TransactionService transactionService) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = {"orchestration-events"},
            groupId = "transaction-service",
            filter = "kafkaEventFilterForTransaction")
    public void handle(MessageEnvelope<?> messageEnvelope) {
        int rowsInserted = this.transactionService.insertIntoProcessedMessages(
                messageEnvelope.getMessageId()
        );
        if (rowsInserted == 0) {
            return;
        }
        if (messageEnvelope.getPayload() instanceof SagaSucceededEvent) {
            SagaSucceededEvent sagaSucceededEvent = (SagaSucceededEvent) messageEnvelope.getPayload();
            if (sagaSucceededEvent.isCustomerOnboardingSaga()) {
                return;
            }
            this.transactionService.buildTransactionSucceededEventAndPublishToOutbox(sagaSucceededEvent,
                    messageEnvelope.getMessageId());
        } else if (messageEnvelope.getPayload() instanceof SagaFailedEvent) {
            SagaFailedEvent sagaFailedEvent = (SagaFailedEvent) messageEnvelope.getPayload();
            if (sagaFailedEvent.isCustomerOnboardingSaga()) {
                return;
            }
            this.transactionService.buildTransactionFailedEventAndPublishToOutbox(sagaFailedEvent,
                    messageEnvelope.getMessageId());
        }
    }
}
