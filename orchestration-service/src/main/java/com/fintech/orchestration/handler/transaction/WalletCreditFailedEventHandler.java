package com.fintech.orchestration.handler.transaction;

import com.fintech.common.event.WalletCreditFailedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class WalletCreditFailedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public WalletCreditFailedEventHandler(ProcessedMessagesRepository processedMessagesRepository,
                                          SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    @Override
    public String eventType() {
        return "WalletCreditFailedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            WalletCreditFailedEvent walletCreditFailedEvent = (WalletCreditFailedEvent)
                    envelope.getPayload();
            this.sagaLifeCycleManager.fail(envelope,
                    walletCreditFailedEvent.retryable(),
                    walletCreditFailedEvent.reason());
        }
    }
}
