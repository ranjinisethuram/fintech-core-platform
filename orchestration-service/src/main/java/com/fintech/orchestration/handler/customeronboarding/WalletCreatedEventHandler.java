package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.event.WalletCreatedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.common.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.orchestration.domain.*;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class WalletCreatedEventHandler implements MessageHandler {
    private final SagaService sagaService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public WalletCreatedEventHandler(SagaService sagaService, ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.sagaService = sagaService;
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
    }

    @Override
    public String eventType() {
        return "WalletCreatedEvent";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            UUID sagaId = UUID.fromString(envelope.getSagaId());
            WalletCreatedEvent walletCreatedEvent = (WalletCreatedEvent) envelope.getPayload();
            SagaContext sagaContext = sagaService
                    .fetchSagaContext(sagaId);
            CustomerOnboardingContext customerOnboardingContext = null;
            if(sagaContext != null){
                customerOnboardingContext = sagaService.
                        loadSagaContext(sagaContext.getContextJson(), CustomerOnboardingContext.class);
                customerOnboardingContext.setWalletId(UUID.fromString(walletCreatedEvent.walletId()));
                customerOnboardingContext.setCurrency(walletCreatedEvent.currency());
            }

            Saga updatedSaga = this.sagaLifeCycleManager.resume(envelope, StepId.CREATE_WALLET,
                    customerOnboardingContext);
            this.orchestrationEngine.process(updatedSaga, envelope.getMessageId());
        }
    }
}
