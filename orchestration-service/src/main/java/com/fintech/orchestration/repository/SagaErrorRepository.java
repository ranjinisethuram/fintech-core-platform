package com.fintech.orchestration.repository;

import com.fintech.orchestration.domain.SagaError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaErrorRepository extends JpaRepository<SagaError, String> {
}
