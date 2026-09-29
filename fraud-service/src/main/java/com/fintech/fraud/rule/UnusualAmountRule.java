package com.fintech.fraud.rule;

import com.fintech.fraudcontract.dto.HistoricalTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class UnusualAmountRule implements FraudRule {
    public static final int SCORE = 20;

    @Override
    public String getName() {
        return "UnusualAmountRule";
    }

    public int evaluate(BigDecimal amount, List<HistoricalTransaction> historicalTransactions) {
        if (amount == null || historicalTransactions == null || historicalTransactions.isEmpty()) {
            return 0;
        }

        BigDecimal average = historicalTransactions.stream()
                .map(HistoricalTransaction::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicalTransactions.size()), 2, RoundingMode.HALF_UP);

        if (average.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        return amount.compareTo(average.multiply(BigDecimal.valueOf(3))) > 0 ? SCORE : 0;
    }
}
