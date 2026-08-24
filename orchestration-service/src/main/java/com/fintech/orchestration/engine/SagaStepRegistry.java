package com.fintech.orchestration.engine;

import com.fintech.orchestration.domain.StepId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SagaStepRegistry {

    private final Map<StepId, SagaStep> steps;

    public SagaStepRegistry(List<SagaStep> sagaStepImplementations) {
        this.steps = sagaStepImplementations.stream()
                .collect(Collectors.toMap(
                        SagaStep::stepId,
                        Function.identity()));
    }

    public SagaStep getStep(StepId stepId) {
        return steps.get(stepId);
    }
}
