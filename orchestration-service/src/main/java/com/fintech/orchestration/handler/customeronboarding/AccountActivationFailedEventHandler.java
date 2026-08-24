package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.event.AccountActivationFailedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountActivationFailedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public AccountActivationFailedEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    @Override
    public String eventType() {
        return "AccountActivationFailedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            AccountActivationFailedEvent accountActivationFailedEvent =
                    (AccountActivationFailedEvent)envelope.getPayload();
            this.sagaLifeCycleManager.fail(envelope.getSagaId(),
                    accountActivationFailedEvent.retryable(),
                    accountActivationFailedEvent.reason());
        }
    }
}
