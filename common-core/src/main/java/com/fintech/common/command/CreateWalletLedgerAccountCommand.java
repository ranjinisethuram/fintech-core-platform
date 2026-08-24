package com.fintech.common.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.Currency;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateWalletLedgerAccountCommand(
        UUID ledgerAccountId,
        UUID walletId,
        UUID accountId,
        UUID customerId,
        Currency currency
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId") // Maps getAggregateId() result to JSON
    public String getAggregateId() {
        return ledgerAccountId.toString();
    }

    @Override
    @JsonProperty("aggregateType") // Maps getAggregateType() result to JSON
    public AggregateType getAggregateType() {
        return AggregateType.LEDGER;
    }
}
