package com.fintech.orchestration.engine;

import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.SagaContextType;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SagaLifeCycleManager {

    private final SagaService sagaService;

    public SagaLifeCycleManager(SagaService sagaService) {
        this.sagaService = sagaService;
    }

    public <T> Saga initiateSaga(MessageEnvelope<?> envelope, SagaContextType contextType,
                                 Object sagaContext) {
        Saga sagaSaved = this.sagaService
                .startSaga(envelope.getCorrelationId(), envelope.getAggregateId());
        sagaService.createSagaContext(sagaSaved.getSagaId(),
                contextType,sagaContext);
        return sagaSaved;
    }

    public <T> Saga resume(MessageEnvelope<?> envelope, StepId completedStep,
                           T sagaContext){
        Saga saga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if(saga != null){
            saga = this.sagaService.updateSagaState(saga, completedStep);
            if(sagaContext != null){
                updateSagaContext(saga, sagaContext);
            }
        }else{
            //throw exception could not find saga.
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        return saga;
    }

    public void fail(String sagaId, boolean isRetryable, String errorMessage){
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(sagaId));
        if(currentSaga == null){
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        if(isRetryable && this.sagaService.isSagaRetryable(currentSaga.getRetryCount())){
            //mark the saga for retry
            this.sagaService.updateSagaForRetry(currentSaga);
        }else{
            //mark saga as failed
            this.sagaService.updateSagaFailed(currentSaga, errorMessage);
        }
    }

    public void compensate(String sagaId) {
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(sagaId));
        if(currentSaga == null){
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        this.sagaService.updateSagaForCompensation(currentSaga);
    }

    public void complete(String sagaId, StepId currentStep){
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(sagaId));
        if(currentSaga == null){
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        this.sagaService.updateSagaComplete(currentSaga,currentStep);
    }

    private <T> void updateSagaContext(Saga saga, T sagaContext){
        SagaContext sagaContextEntity = sagaService
                .fetchSagaContext(saga.getSagaId());
        if(sagaContextEntity == null){
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
        }
        this.sagaService.updateSagaContext(sagaContextEntity,
                sagaContext);
    }
}
