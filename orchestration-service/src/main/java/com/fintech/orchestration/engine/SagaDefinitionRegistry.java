package com.fintech.orchestration.engine;

import com.fintech.orchestration.domain.SagaType;

import java.util.EnumMap;
import java.util.Map;

public class SagaDefinitionRegistry {

    private final Map<SagaType, SagaDefinition> definitions =
            new EnumMap<>(SagaType.class);

    public void register(SagaDefinition definition) {
        if (definitions.containsKey(definition.sagaType())) {
            throw new IllegalStateException(
                    "Saga definition already registered: "
                            + definition.sagaType());
        }

        definitions.put(
                definition.sagaType(),
                definition);
    }

    public SagaDefinition get(SagaType sagaType) {
        SagaDefinition definition =
                definitions.get(sagaType);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "No saga definition found for: " + sagaType);
        }

        return definition;
    }
}
