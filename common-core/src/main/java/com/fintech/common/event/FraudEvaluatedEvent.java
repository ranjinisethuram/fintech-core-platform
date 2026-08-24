package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudEvaluatedEvent(
        String transactionId,
        int totalScore,
        String riskLevel,
        List<String> triggeredRules,
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
