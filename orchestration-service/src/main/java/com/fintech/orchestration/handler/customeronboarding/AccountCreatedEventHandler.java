package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.event.AccountCreatedEvent;
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
public class AccountCreatedEventHandler implements MessageHandler {

    private final SagaService sagaService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public AccountCreatedEventHandler(SagaService sagaService, ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.sagaService = sagaService;
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
    }

    @Override
    public String eventType() {
        return "AccountCreatedEvent";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
//            UUID sagaId = UUID.fromString(envelope.getSagaId());
//            Saga currentSaga = this.sagaService.fetchSagaById(sagaId);
//            AccountCreatedEvent accountCreatedEvent = (AccountCreatedEvent)envelope.getPayload();
//            if(currentSaga != null){
//                Saga updatedSaga = this.sagaService.updateSagaState(currentSaga,
//                        SagaState.ACCOUNT_CREATED, SagaState.WALLET_CREATION);
//                SagaContext sagaContext = sagaService
//                        .fetchSagaContext(updatedSaga.getSagaId());
//                if(sagaContext == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = sagaService.
//                        loadSagaContext(sagaContext.getContextJson(), CustomerOnboardingContext.class);
//                customerOnboardingContext.setAccountId(accountCreatedEvent.id());
//                customerOnboardingContext.setDefaultAccount(accountCreatedEvent.isDefault());
//                this.sagaService.updateSagaContext(sagaContext,
//                        customerOnboardingContext);
//                this.sagaService.publishCreateWalletCommandToOutbox(updatedSaga,envelope);
//            }else{
//                //throw exception could not find saga.
//                throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
//            }
            UUID sagaId = UUID.fromString(envelope.getSagaId());
            AccountCreatedEvent accountCreatedEvent = (AccountCreatedEvent)envelope.getPayload();
            SagaContext sagaContext = sagaService
                        .fetchSagaContext(sagaId);
            CustomerOnboardingContext customerOnboardingContext = null;
            if(sagaContext != null){
                customerOnboardingContext = sagaService.
                        loadSagaContext(sagaContext.getContextJson(), CustomerOnboardingContext.class);
                customerOnboardingContext.setAccountId(UUID.fromString(accountCreatedEvent.accountId()));
                customerOnboardingContext.setDefaultAccount(accountCreatedEvent.isDefault());
            }

            Saga updatedSaga = this.sagaLifeCycleManager.resume(envelope, StepId.CREATE_ACCOUNT,
                    customerOnboardingContext);
            this.orchestrationEngine.process(updatedSaga, envelope.getMessageId());
        }
    }
}
