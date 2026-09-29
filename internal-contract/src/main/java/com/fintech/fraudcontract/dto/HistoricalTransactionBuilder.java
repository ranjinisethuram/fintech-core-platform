package com.fintech.fraudcontract.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HistoricalTransactionBuilder {

    private final List<HistoricalTransaction> transactions = new ArrayList<>();

    public HistoricalTransactionBuilder addTransaction(BigDecimal amount, Instant timestamp) {
        if (amount != null && timestamp != null) {
            transactions.add(new HistoricalTransaction(amount, timestamp));
        }
        return this;
    }

    public HistoricalTransactionBuilder addTransactions(List<HistoricalTransaction> txns) {
        if (txns != null) {
            transactions.addAll(txns);
        }
        return this;
    }

    public List<HistoricalTransaction> build() {
        return new ArrayList<>(transactions);
    }

    /**
     * Filters transactions within the last N minutes
     *
     * @param minutes number of minutes from now
     * @param referenceTime the reference time to calculate from
     * @return filtered list of transactions within the time window
     */
    public List<HistoricalTransaction> filterByTimeWindow(int minutes, Instant referenceTime) {
        if (referenceTime == null) {
            referenceTime = Instant.now();
        }

        final Instant cutoff = referenceTime.minusSeconds((long) minutes * 60);
        return transactions.stream()
                .filter(txn -> txn.timestamp() != null && !txn.timestamp().isBefore(cutoff))
                .toList();
    }

    /**
     * Calculates the average transaction amount
     *
     * @return average amount, or BigDecimal.ZERO if no transactions
     */
    public BigDecimal calculateAverage() {
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return transactions.stream()
                .map(HistoricalTransaction::amount)
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Counts transactions within the last N minutes
     *
     * @param minutes number of minutes from now
     * @param referenceTime the reference time to calculate from
     * @return count of transactions within the time window
     */
    public long countInTimeWindow(int minutes, Instant referenceTime) {
        return filterByTimeWindow(minutes, referenceTime).size();
    }
}
