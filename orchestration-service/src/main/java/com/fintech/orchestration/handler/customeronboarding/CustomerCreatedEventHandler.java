package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.event.CustomerCreatedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.common.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.common.domain.SagaContextType;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class CustomerCreatedEventHandler implements MessageHandler {

    private final SagaService sagaService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public CustomerCreatedEventHandler(SagaService sagaService
            , ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.sagaService = sagaService;
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
    }

    @Override
    public String eventType() {
        return "CustomerCreatedEvent";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(),Instant.now());
        if(rowsInserted != 0) {
            CustomerCreatedEvent customerCreatedEvent = (CustomerCreatedEvent)
                    envelope.getPayload();
//            Saga sagaSaved = sagaService.startSaga(envelope.getCorrelationId(),
//                    customerCreatedEvent.getAggregateId(), SagaState.CUSTOMER_CREATED, SagaState.ACCOUNT_CREATION);
//            CustomerOnboardingContext customerOnboardingContext = new CustomerOnboardingContext();
//            customerOnboardingContext.setCustomerId(customerCreatedEvent.customerId());
//            sagaService.createSagaContext(sagaSaved.getSagaId(),
//                    SagaContextType.CUSTOMER_ONBOARDING,customerOnboardingContext);
//            sagaService.publishCreateAccountCommandToOutbox(sagaSaved, envelope);

            //Call sagaLifeCycleManager.start()
            //It will persist saga and point the nextStepIndex to 0.
            //Persist customer onboardingcontext as above.
            //Call orchestrationEngine.process(saga,envelope) which will execute current step and advance it.
            CustomerOnboardingContext customerOnboardingContext = new CustomerOnboardingContext();
            customerOnboardingContext.setCustomerId(UUID.fromString(
                    customerCreatedEvent.customerId()));
            customerOnboardingContext.setDefaultAccount(true);
            Saga savedSaga = this.sagaLifeCycleManager.initiateSaga(envelope,
                    SagaContextType.CUSTOMER_ONBOARDING, customerOnboardingContext);
            this.orchestrationEngine.process(savedSaga, envelope.getMessageId());
        }
    }
}
