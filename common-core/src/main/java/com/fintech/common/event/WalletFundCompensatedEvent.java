package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.Currency;
import com.fintech.common.domain.TransactionType;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletFundCompensatedEvent(
        String accountId,
        String transactionId,
        BigDecimal amount,
        Currency currency,
        TransactionType transactionType,
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
