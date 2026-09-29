package com.fintech.analytics.controller;

import com.fintech.analytics.domain.FraudDailySummaryEntity;
import com.fintech.analytics.domain.TransactionDailySummaryEntity;
import com.fintech.analytics.repository.FraudDailySummaryRepository;
import com.fintech.analytics.repository.TransactionDailySummaryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final TransactionDailySummaryRepository transactionRepo;
    private final FraudDailySummaryRepository fraudRepo;

    public AnalyticsController(TransactionDailySummaryRepository transactionRepo, FraudDailySummaryRepository fraudRepo) {
        this.transactionRepo = transactionRepo;
        this.fraudRepo = fraudRepo;
    }

    @GetMapping("/transactions/daily")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:summary')")
    public ResponseEntity<?> getTransactionDaily(@RequestParam(value = "date", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate d = date == null ? LocalDate.now() : date;
        TransactionDailySummaryEntity entity = transactionRepo.findBySummaryDate(d).orElse(null);
        if (entity == null) {
            Map<String,Object> empty = Map.of(
                    "date", d.toString(),
                    "transactionCount", 0,
                    "successfulCount", 0,
                    "failedCount", 0,
                    "totalAmount", BigDecimal.ZERO
            );
            return ResponseEntity.ok(empty);
        }
        Map<String,Object> resp = Map.of(
                "date", entity.getSummaryDate().toString(),
                "transactionCount", entity.getTransactionCount(),
                "successfulCount", entity.getSuccessfulCount(),
                "failedCount", entity.getFailedCount(),
                "totalAmount", entity.getTotalAmount() == null ? BigDecimal.ZERO : entity.getTotalAmount()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/fraud/daily")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_fraud:summary')")
    public ResponseEntity<?> getFraudDaily(@RequestParam(value = "date", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate d = date == null ? LocalDate.now() : date;
        FraudDailySummaryEntity entity = fraudRepo.findBySummaryDate(d).orElse(null);
        if (entity == null) {
            Map<String,Object> empty = Map.of(
                    "date", d.toString(),
                    "evaluatedCount", 0,
                    "lowRiskCount", 0,
                    "mediumRiskCount", 0,
                    "highRiskCount", 0,
                    "criticalRiskCount", 0
            );
            return ResponseEntity.ok(empty);
        }
        Map<String,Object> resp = Map.of(
                "date", entity.getSummaryDate().toString(),
                "evaluatedCount", entity.getEvaluatedCount(),
                "lowRiskCount", entity.getLowRiskCount(),
                "mediumRiskCount", entity.getMediumRiskCount(),
                "highRiskCount", entity.getHighRiskCount(),
                "criticalRiskCount", entity.getCriticalRiskCount()
        );
        return ResponseEntity.ok(resp);
    }
}
