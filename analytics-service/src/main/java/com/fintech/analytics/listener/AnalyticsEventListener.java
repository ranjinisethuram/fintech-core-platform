package com.fintech.analytics.listener;

import com.fintech.analytics.repository.ProcessedMessagesRepository;
import com.fintech.analytics.service.AnalyticsService;
import com.fintech.common.event.FraudEvaluatedEvent;
import com.fintech.common.event.TransactionSagaFailedEvent;
import com.fintech.common.event.TransactionSagaSucceededEvent;
import com.fintech.common.messaging.MessageEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AnalyticsEventListener {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final AnalyticsService analyticsService;

    public AnalyticsEventListener(ProcessedMessagesRepository processedMessagesRepository,
                                  AnalyticsService analyticsService) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = {"transaction-events"}, groupId = "analytics-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionEvents(MessageEnvelope<?> messageEnvelope) {
        int rowsInserted = processedMessagesRepository.insert(messageEnvelope.getMessageId(), Instant.now());
        if (rowsInserted == 0) {
            return;
        }
        Object payload = messageEnvelope.getPayload();
        if (payload instanceof TransactionSagaSucceededEvent) {
            analyticsService.recordTransactionSucceeded((TransactionSagaSucceededEvent) payload);
        } else if (payload instanceof TransactionSagaFailedEvent) {
            analyticsService.recordTransactionFailed((TransactionSagaFailedEvent) payload);
        }
    }

    @KafkaListener(topics = {"fraud-events"}, groupId = "analytics-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleFraud(MessageEnvelope<?> messageEnvelope) {
        int rowsInserted = processedMessagesRepository.insert(messageEnvelope.getMessageId(), Instant.now());
        if (rowsInserted == 0) {
            return;
        }
        Object payload = messageEnvelope.getPayload();
        if (payload instanceof FraudEvaluatedEvent) {
            analyticsService.recordFraudEvaluated((FraudEvaluatedEvent) payload);
        }
    }
}
