package com.fintech.analytics.repository;

import com.fintech.analytics.domain.FraudDailySummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FraudDailySummaryRepository extends JpaRepository<FraudDailySummaryEntity, LocalDate> {
    Optional<FraudDailySummaryEntity> findBySummaryDate(LocalDate date);
}
