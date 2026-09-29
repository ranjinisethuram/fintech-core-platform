package com.fintech.orchestration.handler.transaction;

import com.fintech.common.domain.TransactionType;
import com.fintech.common.event.LedgerEntriesCreationFailedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class LedgerEntriesCreationFailedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaService sagaService;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public LedgerEntriesCreationFailedEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaService sagaService, SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaService = sagaService;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    @Override
    public String eventType() {
        return "LedgerEntriesCreationFailedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            LedgerEntriesCreationFailedEvent ledgerEntriesCreationFailedEvent = (LedgerEntriesCreationFailedEvent)
                    envelope.getPayload();
            this.sagaLifeCycleManager.fail(envelope,
                    ledgerEntriesCreationFailedEvent.retryable(),
                    ledgerEntriesCreationFailedEvent.reason());
        }
    }
}
