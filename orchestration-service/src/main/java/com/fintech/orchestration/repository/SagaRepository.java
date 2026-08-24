package com.fintech.orchestration.repository;

import com.fintech.orchestration.domain.Saga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SagaRepository extends JpaRepository<Saga, UUID> {

    @Query(
            value = """
            SELECT * FROM saga
            WHERE saga_status IN ('STARTED','IN_PROGRESS','WAITING_RETRY')
            AND timeout_at <= NOW()
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    public List<Saga> fetchStuckSagas(@Param("batchSize") int batchSize);

    @Query(
            value = """
            SELECT * FROM saga
            WHERE saga_status = 'WAITING_RETRY'
            AND retry_at <= NOW()
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    public List<Saga> fetchWaitingSagas(@Param("batchSize") int batchSize);

    public boolean existsByAggregateId(String aggregateId);
}
