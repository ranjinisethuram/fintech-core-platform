package com.fintech.orchestration.engine;

import com.fintech.orchestration.domain.SagaType;
import com.fintech.orchestration.domain.StepId;

import java.util.List;

public record SagaDefinition(
        SagaType sagaType,
        List<StepId> stepIds
) {
}
