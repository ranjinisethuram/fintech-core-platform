package com.fintech.orchestration.repository;

import com.fintech.orchestration.domain.SagaRecoveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SagaRecoveryAttemptRepository extends JpaRepository<SagaRecoveryAttempt, UUID> {
}
