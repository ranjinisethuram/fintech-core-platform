package com.fintech.analytics.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDate;

@Entity
@Table(name = "fraud_daily_summary")
public class FraudDailySummaryEntity {

    @Id
    @Column(name = "summary_date")
    private LocalDate summaryDate;

    @Column(name = "evaluated_count")
    private long evaluatedCount;

    @Column(name = "low_risk_count")
    private long lowRiskCount;

    @Column(name = "medium_risk_count")
    private long mediumRiskCount;

    @Column(name = "high_risk_count")
    private long highRiskCount;

    @Column(name = "critical_risk_count")
    private long criticalRiskCount;

    public LocalDate getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(LocalDate summaryDate) {
        this.summaryDate = summaryDate;
    }

    public long getEvaluatedCount() {
        return evaluatedCount;
    }

    public void setEvaluatedCount(long evaluatedCount) {
        this.evaluatedCount = evaluatedCount;
    }

    public long getLowRiskCount() {
        return lowRiskCount;
    }

    public void setLowRiskCount(long lowRiskCount) {
        this.lowRiskCount = lowRiskCount;
    }

    public long getMediumRiskCount() {
        return mediumRiskCount;
    }

    public void setMediumRiskCount(long mediumRiskCount) {
        this.mediumRiskCount = mediumRiskCount;
    }

    public long getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(long highRiskCount) {
        this.highRiskCount = highRiskCount;
    }

    public long getCriticalRiskCount() {
        return criticalRiskCount;
    }

    public void setCriticalRiskCount(long criticalRiskCount) {
        this.criticalRiskCount = criticalRiskCount;
    }
}
