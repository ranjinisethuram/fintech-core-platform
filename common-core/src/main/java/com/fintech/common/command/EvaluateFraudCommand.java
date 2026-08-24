package com.fintech.common.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluateFraudCommand(
        UUID transactionId,
        BigDecimal amount,
        Instant transactionTimestamp,
        List<HistoricalTransaction> historicalTransactions
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId")
    public String getAggregateId() {
        return transactionId.toString();
    }

    @Override
    @JsonProperty("aggregateType")
    public AggregateType getAggregateType() {
        return AggregateType.TRANSACTION;
    }

    public record HistoricalTransaction(BigDecimal amount, Instant timestamp) {
    }
}
