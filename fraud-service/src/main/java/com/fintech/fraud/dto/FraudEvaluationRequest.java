package com.fintech.fraud.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FraudEvaluationRequest(
        @NotNull(message = "amount is required") BigDecimal amount,
        @NotNull(message = "transactionTimestamp is required") Instant transactionTimestamp,
        List<HistoricalTransaction> historicalTransactions
) {
    public FraudEvaluationRequest {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (transactionTimestamp == null) {
            transactionTimestamp = Instant.now();
        }
        if (historicalTransactions == null) {
            historicalTransactions = List.of();
        }
    }
}
