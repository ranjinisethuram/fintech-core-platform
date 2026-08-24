package com.fintech.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.Currency;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletFundTransferredEvent(
        String fromAccountId,
        String toAccountId,
        String transactionId,
        BigDecimal amount,
        Currency currency,
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
