package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class AccountActivatedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public AccountActivatedEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager) {
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "AccountActivatedEvent";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            this.sagaLifeCycleManager.complete(envelope, StepId.ACTIVATE_ACCOUNT);
        }

    }
}
