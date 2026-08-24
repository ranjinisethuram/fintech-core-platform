package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.time.Instant;
import java.util.UUID;

public record TransactionSagaFailedEvent(
        String transactionId,
        String sourceAccountId,
        String destinationAccountId,
        String sagaId,
        String errorCode,
        String reason,
        boolean retryable,
        Instant occurredAt
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId") // Maps getAggregateId() result to JSON
    public String getAggregateId() {
        return transactionId.toString();
    }

    @Override
    @JsonProperty("aggregateType") // Maps getAggregateType() result to JSON
    public AggregateType getAggregateType() {
        return AggregateType.TRANSACTION;
    }
}
