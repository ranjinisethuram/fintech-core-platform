package com.fintech.analytics.service;

import com.fintech.analytics.domain.FraudDailySummaryEntity;
import com.fintech.analytics.domain.TransactionDailySummaryEntity;
import com.fintech.analytics.repository.FraudDailySummaryRepository;
import com.fintech.analytics.repository.ProcessedMessagesRepository;
import com.fintech.analytics.repository.TransactionDailySummaryRepository;
import com.fintech.common.event.FraudEvaluatedEvent;
import com.fintech.common.event.TransactionSagaFailedEvent;
import com.fintech.common.event.TransactionSagaSucceededEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

@Service
public class AnalyticsService {

    private final TransactionDailySummaryRepository transactionRepo;
    private final FraudDailySummaryRepository fraudRepo;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public AnalyticsService(TransactionDailySummaryRepository transactionRepo,
                            FraudDailySummaryRepository fraudRepo,
                            ProcessedMessagesRepository processedMessagesRepository) {
        this.transactionRepo = transactionRepo;
        this.fraudRepo = fraudRepo;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Transactional
    public void recordTransactionSucceeded(TransactionSagaSucceededEvent event) {
        LocalDate date = LocalDate.now();
        TransactionDailySummaryEntity summary = transactionRepo.findBySummaryDate(date)
                .orElseGet(() -> {
                    TransactionDailySummaryEntity s = new TransactionDailySummaryEntity();
                    s.setSummaryDate(date);
                    s.setTransactionCount(0);
                    s.setSuccessfulCount(0);
                    s.setFailedCount(0);
                    s.setTotalAmount(BigDecimal.ZERO);
                    return s;
                });
        summary.setTransactionCount(summary.getTransactionCount() + 1);
        summary.setSuccessfulCount(summary.getSuccessfulCount() + 1);
        BigDecimal amount = event.amount() == null ? BigDecimal.ZERO : event.amount();
        summary.setTotalAmount(summary.getTotalAmount().add(amount));
        transactionRepo.save(summary);
    }

    @Transactional
    public void recordTransactionFailed(TransactionSagaFailedEvent event) {
        LocalDate date = event.occurredAt() == null ? LocalDate.now() : LocalDate.ofInstant(event.occurredAt(), ZoneId.systemDefault());
        TransactionDailySummaryEntity summary = transactionRepo.findBySummaryDate(date)
                .orElseGet(() -> {
                    TransactionDailySummaryEntity s = new TransactionDailySummaryEntity();
                    s.setSummaryDate(date);
                    s.setTransactionCount(0);
                    s.setSuccessfulCount(0);
                    s.setFailedCount(0);
                    s.setTotalAmount(BigDecimal.ZERO);
                    return s;
                });
        summary.setTransactionCount(summary.getTransactionCount() + 1);
        summary.setFailedCount(summary.getFailedCount() + 1);
        transactionRepo.save(summary);
    }

    @Transactional
    public void recordFraudEvaluated(FraudEvaluatedEvent event) {
        LocalDate date = event.occurredAt() == null ? LocalDate.now() : LocalDate.ofInstant(event.occurredAt(), ZoneId.systemDefault());
        FraudDailySummaryEntity summary = fraudRepo.findBySummaryDate(date)
                .orElseGet(() -> {
                    FraudDailySummaryEntity s = new FraudDailySummaryEntity();
                    s.setSummaryDate(date);
                    s.setEvaluatedCount(0);
                    s.setLowRiskCount(0);
                    s.setMediumRiskCount(0);
                    s.setHighRiskCount(0);
                    s.setCriticalRiskCount(0);
                    return s;
                });
        summary.setEvaluatedCount(summary.getEvaluatedCount() + 1);
        String risk = event.riskLevel() == null ? "LOW" : event.riskLevel();
        switch (risk.toUpperCase()) {
            case "LOW" -> summary.setLowRiskCount(summary.getLowRiskCount() + 1);
            case "MEDIUM" -> summary.setMediumRiskCount(summary.getMediumRiskCount() + 1);
            case "HIGH" -> summary.setHighRiskCount(summary.getHighRiskCount() + 1);
            case "CRITICAL" -> summary.setCriticalRiskCount(summary.getCriticalRiskCount() + 1);
            default -> summary.setLowRiskCount(summary.getLowRiskCount() + 1);
        }
        fraudRepo.save(summary);
    }
}
