package com.fintech.fraud.rule;

import com.fintech.fraud.dto.HistoricalTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HighVelocityRule implements FraudRule {
    public static final int SCORE = 30;

    @Override
    public String getName() {
        return "HighVelocityRule";
    }

    public int evaluate(List<HistoricalTransaction> historicalTransactions, Instant referenceTimestamp) {
        return evaluate(historicalTransactions, referenceTimestamp, null);
    }

    public int evaluate(List<HistoricalTransaction> historicalTransactions, Instant referenceTimestamp, BigDecimal currentAmount) {
        List<HistoricalTransaction> allTransactions = new ArrayList<>();
        if (historicalTransactions != null) {
            allTransactions.addAll(historicalTransactions);
        }
        if (currentAmount != null && referenceTimestamp != null) {
            allTransactions.add(new HistoricalTransaction(currentAmount, referenceTimestamp));
        }

        if (allTransactions.isEmpty() || referenceTimestamp == null) {
            return 0;
        }

        Instant cutoff = referenceTimestamp.minus(10, ChronoUnit.MINUTES);
        long transactionsInWindow = allTransactions.stream()
                .filter(transaction -> transaction != null && transaction.timestamp() != null)
                .filter(transaction -> !transaction.timestamp().isBefore(cutoff))
                .count();

        return transactionsInWindow > 5 ? SCORE : 0;
    }
}
