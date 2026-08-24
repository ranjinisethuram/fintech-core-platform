package com.fintech.orchestration.handler.customeronboarding;

import com.fintech.common.event.LedgerAccountCreatedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class LedgerAccountCreatedEventHandler implements MessageHandler {

    private final SagaService sagaService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public LedgerAccountCreatedEventHandler(SagaService sagaService, ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.sagaService = sagaService;
        this.processedMessagesRepository = processedMessagesRepository;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
    }

    @Override
    public String eventType() {
        return "LedgerAccountCreatedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
//            UUID sagaId = UUID.fromString(envelope.getSagaId());
//            Saga currentSaga = this.sagaService.fetchSagaById(sagaId);
//            LedgerAccountCreatedEvent ledgerAccountCreatedEvent = (LedgerAccountCreatedEvent) envelope.getPayload();
//            if(currentSaga != null){
//                Saga updatedSaga = this.sagaService.updateSagaState(currentSaga,
//                        SagaState.LEDGER_ACCOUNT_CREATED, SagaState.ACCOUNT_ACTIVATION);
//                SagaContext sagaContext = sagaService
//                        .fetchSagaContext(updatedSaga.getSagaId());
//                if(sagaContext == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = sagaService.
//                        loadSagaContext(sagaContext.getContextJson(), CustomerOnboardingContext.class);
//                customerOnboardingContext.setLedgerAccountId(ledgerAccountCreatedEvent.id());
//                this.sagaService.updateSagaContext(sagaContext,
//                        customerOnboardingContext);
//                this.sagaService.publishActivateAccountCommandToOutbox
//                        (updatedSaga,envelope);
//            }else{
//                //throw exception could not find saga.
//                throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
//            }
            UUID sagaId = UUID.fromString(envelope.getSagaId());
            LedgerAccountCreatedEvent ledgerAccountCreatedEvent = (LedgerAccountCreatedEvent) envelope.getPayload();
            SagaContext sagaContext = sagaService
                    .fetchSagaContext(sagaId);
            CustomerOnboardingContext customerOnboardingContext = null;
            if(sagaContext != null){
                customerOnboardingContext = sagaService.
                        loadSagaContext(sagaContext.getContextJson(), CustomerOnboardingContext.class);
                customerOnboardingContext.setLedgerAccountId(
                        UUID.fromString(ledgerAccountCreatedEvent.ledgerId()));
            }

            Saga updatedSaga = this.sagaLifeCycleManager.resume(envelope, StepId.CREATE_LEDGER_ACCOUNT,
                    customerOnboardingContext);
            this.orchestrationEngine.process(updatedSaga, envelope.getMessageId());
        }
    }
}
