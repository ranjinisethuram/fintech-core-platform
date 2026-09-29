package com.fintech.orchestration.handler.transaction;

import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class WalletFundTransferredEventHandler implements MessageHandler {
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public WalletFundTransferredEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }
    @Override
    public String eventType() {
        return "WalletFundTransferredEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            this.sagaLifeCycleManager.
                    complete(envelope, StepId.FUND_TRANSFER);
        }
    }
}
