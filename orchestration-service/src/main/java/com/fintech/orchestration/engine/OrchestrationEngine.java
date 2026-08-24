package com.fintech.orchestration.engine;

import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.*;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrchestrationEngine {

    private final SagaDefinitionRegistry sagaDefinitionRegistry;
    private final SagaStepRegistry sagaStepRegistry;
    private final SagaService sagaService;

    public OrchestrationEngine(SagaDefinitionRegistry sagaDefinitionRegistry, SagaStepRegistry sagaStepRegistry, SagaService sagaService) {
        this.sagaDefinitionRegistry = sagaDefinitionRegistry;
        this.sagaStepRegistry = sagaStepRegistry;
        this.sagaService = sagaService;
    }

    public void process(Saga saga, String messageId) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
        SagaType sagaType = getSagaType(sagaContextEntity);
        SagaStep nextStep = fetchNextStep(saga.getNextStepIndex(), sagaType);
        nextStep.execute(saga,messageId);
    }

    public void recover(Saga saga, SagaContext sagaContextEntity, UUID sagaRecoveryId) {
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
