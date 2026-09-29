package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.SagaContextType;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.time.Instant;

public record SagaFailedEvent(
        String sagaId,
        String sagaContext,
        SagaContextType sagaContextType,
        String reason,
        boolean isCompensated,
        Instant occurredAt
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId") // Maps getAggregateId() result to JSON
    public String getAggregateId() {
        return sagaId;
    }

    @Override
    @JsonProperty("aggregateType") // Maps getAggregateType() result to JSON
    public AggregateType getAggregateType() {
        return AggregateType.ORCHESTRATION;
    }

    public boolean isCustomerOnboardingSaga(){
        return sagaContextType.equals(SagaContextType.CUSTOMER_ONBOARDING);
    }
}
