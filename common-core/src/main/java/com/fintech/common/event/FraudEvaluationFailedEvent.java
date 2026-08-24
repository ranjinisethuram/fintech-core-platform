package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudEvaluationFailedEvent(
        String transactionId,
        String errorCode,
        String reason,
        boolean retryable,
        Instant occurredAt
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId")
    public String getAggregateId() {
        return transactionId;
    }

    @Override
    @JsonProperty("aggregateType")
    public AggregateType getAggregateType() {
        return AggregateType.TRANSACTION;
    }
}
