package com.fintech.analytics.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_daily_summary")
public class TransactionDailySummaryEntity {

    @Id
    @Column(name = "summary_date")
    private LocalDate summaryDate;

    @Column(name = "transaction_count")
    private long transactionCount;

    @Column(name = "successful_count")
    private long successfulCount;

    @Column(name = "failed_count")
    private long failedCount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    public LocalDate getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(LocalDate summaryDate) {
        this.summaryDate = summaryDate;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public long getSuccessfulCount() {
        return successfulCount;
    }

    public void setSuccessfulCount(long successfulCount) {
        this.successfulCount = successfulCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
