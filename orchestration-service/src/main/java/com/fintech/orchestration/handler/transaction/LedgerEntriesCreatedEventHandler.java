package com.fintech.orchestration.handler.transaction;

import com.fintech.common.event.LedgerEntriesCreatedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaState;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class LedgerEntriesCreatedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public LedgerEntriesCreatedEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
    }

    @Override
    public String eventType() {
        return "LedgerEntriesCreatedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            Saga updatedSaga = this.sagaLifeCycleManager.resume(envelope, StepId.CREATE_LEDGER_ENTRIES,
                    null);
            this.orchestrationEngine.process(updatedSaga, envelope.getMessageId());
        }
    }
}
