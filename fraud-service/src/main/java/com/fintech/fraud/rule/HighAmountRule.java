package com.fintech.fraud.rule;

import java.math.BigDecimal;

public class HighAmountRule implements FraudRule {
    public static final int SCORE = 40;
    private static final BigDecimal THRESHOLD = new BigDecimal("100000");

    @Override
    public String getName() {
        return "HighAmountRule";
    }

    public int evaluate(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.compareTo(THRESHOLD) > 0 ? SCORE : 0;
    }
}
