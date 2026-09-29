package com.fintech.orchestration.engine;

import com.fintech.common.domain.SagaContextType;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.*;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrchestrationEngine {

    private final SagaDefinitionRegistry sagaDefinitionRegistry;
    private final SagaStepRegistry sagaStepRegistry;
    private final SagaService sagaService;
    private final SagaLifeCycleManager sagaLifeCycleManager;

    public OrchestrationEngine(SagaDefinitionRegistry sagaDefinitionRegistry,
                               SagaStepRegistry sagaStepRegistry,
                               SagaService sagaService, SagaLifeCycleManager sagaLifeCycleManager) {
        this.sagaDefinitionRegistry = sagaDefinitionRegistry;
        this.sagaStepRegistry = sagaStepRegistry;
        this.sagaService = sagaService;
        this.sagaLifeCycleManager = sagaLifeCycleManager;
    }

    public void process(Saga saga, String messageId) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
        SagaType sagaType = getSagaType(sagaContextEntity);
        SagaStep nextStep = fetchNextStep(saga.getNextStepIndex(), sagaType);
        nextStep.execute(saga,messageId);
    }

    public void recover(Saga saga, SagaContext sagaContextEntity, UUID sagaRecoveryId) {
        // If saga is in compensation flow (compensation step index or step present), recover compensation step
        if (saga.getCompensationStep() != null || saga.getCompensationStepIndex() != null) {
            StepId compStepId = saga.getCompensationStep();
            // If compensation step not set, try to rebuild from completed steps using compensationStepIndex
            if (compStepId == null && saga.getCompletedSteps() != null && saga.getCompensationStepIndex() != null) {
                // rebuild compensation list (reverse of completed steps)
                for (int i = saga.getCompletedSteps().size() - 1, idx = 0; i >= 0; i--, idx++) {
                    StepId forward = saga.getCompletedSteps().get(i);
                    if (forward == null) continue;
                    var forwardStep = this.sagaStepRegistry.getStep(forward);
                    if (forwardStep != null) {
                        var comp = forwardStep.compensationStep();
                        if (comp.isPresent() && idx == saga.getCompensationStepIndex()) {
                            compStepId = comp.get();
                            break;
                        }
                    }
                }
            }
            if (compStepId != null) {
                SagaStep compStep = this.sagaStepRegistry.getStep(compStepId);
                if (compStep != null) {
                    compStep.recover(saga, sagaRecoveryId);
                    return;
                }
            }
            // If we couldn't determine compensation step, fallthrough to forward recovery for safety
        }

        SagaType sagaType = getSagaType(sagaContextEntity);
        SagaStep nextStep = fetchNextStep(saga.getNextStepIndex(), sagaType);
        nextStep.recover(saga, sagaRecoveryId);
    }

    private SagaStep fetchNextStep(int nextStepIndex, SagaType sagaType) {
        SagaDefinition sagaDefinition = this.sagaDefinitionRegistry.get(sagaType);
        StepId nextStepId = sagaDefinition.stepIds().get(nextStepIndex);
        return this.sagaStepRegistry.getStep(nextStepId);
    }

    private SagaType getSagaType(SagaContext sagaContextEntity) {
        return sagaContextEntity.getSagaContextType().
                equals(SagaContextType.CUSTOMER_ONBOARDING) ?
                SagaType.valueOf(sagaContextEntity.getSagaContextType().name()) :
                resolveTransactionSagaType(sagaContextEntity);
    }

    private SagaType resolveTransactionSagaType(SagaContext sagaContextEntity) {
        TransactionHandlingContext transactionHandlingContext = this.sagaService.loadSagaContext(sagaContextEntity.getContextJson(),
                TransactionHandlingContext.class);
        String transactionTypeName = transactionHandlingContext.getTransactionType().name();
        return SagaType.valueOf(transactionTypeName);
    }
}
