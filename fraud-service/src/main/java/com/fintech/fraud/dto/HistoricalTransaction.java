package com.fintech.fraud.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record HistoricalTransaction(BigDecimal amount, Instant timestamp) {
    public HistoricalTransaction {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
