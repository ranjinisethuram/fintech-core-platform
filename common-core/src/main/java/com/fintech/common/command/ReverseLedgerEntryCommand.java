package com.fintech.common.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintech.common.domain.Currency;
import com.fintech.common.domain.TransactionType;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.AggregateType;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReverseLedgerEntryCommand(
        UUID paymentId,
        //List<LedgerEntryRequest> ledgerEntryRequests,
        UUID sourceAcountId,
        UUID destinationAccountId,
        BigDecimal amount,
        Currency currency,
        TransactionType transactionType
) implements AggregateMessage {
    @Override
    @JsonProperty("aggregateId") // Maps getAggregateId() result to JSON
    public String getAggregateId() {
        return paymentId.toString();
    }

    @Override
    @JsonProperty("aggregateType") // Maps getAggregateType() result to JSON
    public AggregateType getAggregateType() {
        return AggregateType.TRANSACTION;
    }
}
