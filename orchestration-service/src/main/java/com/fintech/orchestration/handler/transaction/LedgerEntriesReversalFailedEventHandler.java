package com.fintech.orchestration.handler.transaction;

import com.fintech.common.event.LedgerEntriesReversalFailedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class LedgerEntriesReversalFailedEventHandler implements MessageHandler {
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public LedgerEntriesReversalFailedEventHandler(ProcessedMessagesRepository processedMessagesRepository,
                                                   SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    @Override
    public String eventType() {
        return "LedgerEntriesReversalFailedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if (rowsInserted != 0) {
            LedgerEntriesReversalFailedEvent payload = (LedgerEntriesReversalFailedEvent) envelope.getPayload();
            this.sagaLifeCycleManager.compensationFailed(envelope, payload.retryable(), payload.reason());
        }
    }
}
