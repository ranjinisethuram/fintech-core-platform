package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.SagaContextType;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.time.Instant;

public record SagaSucceededEvent(
        String sagaId,
        String sagaContext,
        SagaContextType sagaContextType,
        String aggregateId,
        String aggregateType,
        Instant occurredAt
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId") // Maps getAggregateId() result to JSON
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    @JsonProperty("aggregateType") // Maps getAggregateType() result to JSON
    public AggregateType getAggregateType() {
        return AggregateType.valueOf(aggregateType);
    }
}
