package com.fintech.analytics.repository;

import com.fintech.analytics.domain.TransactionDailySummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TransactionDailySummaryRepository extends JpaRepository<TransactionDailySummaryEntity, LocalDate> {
    Optional<TransactionDailySummaryEntity> findBySummaryDate(LocalDate date);
}
