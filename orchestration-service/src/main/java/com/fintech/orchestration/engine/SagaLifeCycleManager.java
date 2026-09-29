package com.fintech.orchestration.engine;

import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.common.domain.SagaContextType;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SagaLifeCycleManager {

    private final SagaService sagaService;
    private final SagaStepRegistry sagaStepRegistry;

    public SagaLifeCycleManager(SagaService sagaService, SagaStepRegistry sagaStepRegistry) {
        this.sagaService = sagaService;
        this.sagaStepRegistry = sagaStepRegistry;
    }

    public <T> Saga initiateSaga(MessageEnvelope<?> envelope, SagaContextType contextType,
                                 Object sagaContext) {
        Saga sagaSaved = this.sagaService
                .startSaga(envelope.getCorrelationId(), envelope.getAggregateId());
        sagaService.createSagaContext(sagaSaved.getSagaId(),
                contextType, sagaContext);
        return sagaSaved;
    }

    public <T> Saga resume(MessageEnvelope<?> envelope, StepId completedStep,
                           T sagaContext) {
        Saga saga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if (saga != null) {
            saga = this.sagaService.updateSagaState(saga, completedStep);
            if (sagaContext != null) {
                updateSagaContext(saga, sagaContext);
            }
        } else {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        return saga;
    }

    /**
     * Overloaded fail to support retryable failures from downstream services.
     * If retryable and saga is retryable -> mark for retry
     * Else, mark as failed and if there are completed steps with compensation steps,
     * start compensation flow (in reverse order).
     */
    public void fail(MessageEnvelope<?> envelope, boolean retryable, String errorMessage) {
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if (currentSaga == null) {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }

        if (retryable && this.sagaService.isSagaRetryable(currentSaga.getRetryCount())) {
            this.sagaService.updateSagaForRetry(currentSaga);
            return;
        }

        // mark saga as failed
        Saga updatedSaga = this.sagaService.updateSagaFailed(currentSaga, errorMessage);

        // determine compensation steps based on completed forward steps
        List<StepId> completed = updatedSaga.getCompletedSteps();
        if (completed == null || completed.isEmpty()) {
            // no completed steps -> no compensation
            this.sagaService.publishSagaFailedEvent(envelope, updatedSaga, errorMessage, false);
            return;
        }

        // build compensation steps in reverse order of completed steps
        List<StepId> compensationSteps = new ArrayList<>();
        for (int i = completed.size() - 1; i >= 0; i--) {
            StepId forward = completed.get(i);
            if (forward == null) continue;
            var forwardStep = sagaStepRegistry.getStep(forward);
            if (forwardStep != null) {
                forwardStep.compensationStep().ifPresent(compensationSteps::add);
            }
        }

        if (compensationSteps.isEmpty()) {
            // nothing to compensate
            this.sagaService.publishSagaFailedEvent(envelope, updatedSaga, errorMessage, false);
            return;
        }

        // initialize compensation on saga and kick off first compensation step
        // store the compensation step index (index into compensationSteps list)
        // we will store index 0 as first to execute
        updatedSaga.setCompensationStepIndex(0);
        updatedSaga.setCompensationStep(compensationSteps.get(0));
        this.sagaService.updateSagaForCompensation(updatedSaga);

        // execute first compensation step
        SagaStep compStepImpl = this.sagaStepRegistry.getStep(compensationSteps.get(0));
        if (compStepImpl != null) {
            compStepImpl.execute(updatedSaga, envelope.getMessageId());
        } else {
            // no implementation found -> mark compensation failed
            this.sagaService.updateSagaCompensationFailed(updatedSaga, "Compensation step implementation not found");
            this.sagaService.publishSagaFailedEvent(envelope, updatedSaga, errorMessage, false);
        }
    }

    /**
     * Handle compensation failure with retry semantics.
     * If retryable and saga is retryable -> mark for retry so SagaRetryJob will re-run recovery.
     * Else mark compensation as failed and publish saga failed event.
     */
    public void compensationFailed(MessageEnvelope<?> envelope, boolean retryable, String errorMessage) {
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if (currentSaga == null) {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }

        if (retryable && this.sagaService.isSagaRetryable(currentSaga.getRetryCount())) {
            this.sagaService.updateSagaForRetry(currentSaga);
            return;
        }

        Saga updatedSaga = this.sagaService.updateSagaCompensationFailed(currentSaga, errorMessage);
        this.sagaService.publishSagaFailedEvent(envelope, updatedSaga, errorMessage, false);
    }

    /**
     * Called by compensation-success handlers when a compensation step completes successfully.
     * This will progress to the next compensation step (if any) or mark the saga compensated.
     */
    public void compensationStepCompleted(MessageEnvelope<?> envelope, StepId completedCompStep) {
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if (currentSaga == null) {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }

        List<StepId> completed = currentSaga.getCompletedSteps();
        if (completed == null || completed.isEmpty()) {
            // nothing to do - mark compensated
            this.sagaService.updateSagaCompensated(currentSaga);
            this.sagaService.publishSagaFailedEvent(envelope, currentSaga, "Compensation completed", true);
            return;
        }

        // rebuild compensation steps list in same order used during failure (reverse of completed steps)
        List<StepId> compensationSteps = new ArrayList<>();
        for (int i = completed.size() - 1; i >= 0; i--) {
            StepId forward = completed.get(i);
            if (forward == null) continue;
            var forwardStep = sagaStepRegistry.getStep(forward);
            if (forwardStep != null) {
                forwardStep.compensationStep().ifPresent(compensationSteps::add);
            }
        }

        if (compensationSteps.isEmpty()) {
            this.sagaService.updateSagaCompensated(currentSaga);
            this.sagaService.publishSagaFailedEvent(envelope, currentSaga, "Compensation completed", true);
            return;
        }

        Integer idx = currentSaga.getCompensationStepIndex();
        int nextIndex = (idx == null) ? 0 : idx + 1;

        if (nextIndex >= compensationSteps.size()) {
            // all compensated
            this.sagaService.updateSagaCompensated(currentSaga);
            this.sagaService.publishSagaFailedEvent(envelope, currentSaga, "Compensation completed", true);
            return;
        }

        StepId nextCompStep = compensationSteps.get(nextIndex);
        currentSaga.setCompensationStepIndex(nextIndex);
        currentSaga.setCompensationStep(nextCompStep);
        this.sagaService.updateSagaForCompensation(currentSaga);

        SagaStep compStepImpl = this.sagaStepRegistry.getStep(nextCompStep);
        if (compStepImpl != null) {
            compStepImpl.execute(currentSaga, envelope.getMessageId());
        } else {
            this.sagaService.updateSagaCompensationFailed(currentSaga, "Compensation step implementation not found");
            this.sagaService.publishSagaFailedEvent(envelope, currentSaga, "Compensation step implementation not found", false);
        }
    }

    public void complete(MessageEnvelope<?> envelope, StepId currentStep) {
        Saga currentSaga = this.sagaService.fetchSagaById(UUID.fromString(envelope.getSagaId()));
        if (currentSaga == null) {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_NOT_FOUND);
        }
        Saga updatedSaga = this.sagaService.updateSagaComplete(currentSaga, currentStep);
        this.sagaService.publishSagaSucceededEvent(envelope, updatedSaga);
    }

    private <T> void updateSagaContext(Saga saga, T sagaContext) {
        SagaContext sagaContextEntity = sagaService
                .fetchSagaContext(saga.getSagaId());
        if (sagaContextEntity == null) {
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
        }
        this.sagaService.updateSagaContext(sagaContextEntity,
                sagaContext);
    }
}
