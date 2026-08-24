package com.fintech.orchestration.engine;

import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;

import java.util.UUID;

public interface SagaStep {
    StepId stepId();
    void execute(Saga saga, String messageId);
    void recover(Saga saga, UUID sagaRecoveryId);
}
