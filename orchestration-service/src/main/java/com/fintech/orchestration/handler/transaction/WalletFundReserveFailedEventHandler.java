package com.fintech.orchestration.handler.transaction;

import com.fintech.common.event.WalletFundReserveFailedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class WalletFundReserveFailedEventHandler implements MessageHandler {
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public WalletFundReserveFailedEventHandler(ProcessedMessagesRepository processedMessagesRepository,
                           SagaLifeCycleManager sagaLifeCycleManager) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    @Override
    public String eventType() {
        return "WalletFundReserveFailedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            WalletFundReserveFailedEvent walletFundReserveFailedEvent =
                    (WalletFundReserveFailedEvent) envelope.getPayload();
            this.sagaLifeCycleManager.fail(envelope,
                    walletFundReserveFailedEvent.retryable(),
                    walletFundReserveFailedEvent.reason());
        }
    }
}
