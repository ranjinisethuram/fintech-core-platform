package com.fintech.orchestration.repository;

import com.fintech.orchestration.domain.SagaContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SagaContextRepository extends JpaRepository<SagaContext, UUID> {
}
