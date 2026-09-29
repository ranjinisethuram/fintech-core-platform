package com.fintech.orchestration.engine;

import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.StepId;

import java.util.Optional;
import java.util.UUID;

public interface SagaStep {
    StepId stepId();
    void execute(Saga saga, String messageId);
    void recover(Saga saga, UUID sagaRecoveryId);
    default Optional<StepId> compensationStep() {
        return Optional.empty();
    }
}
