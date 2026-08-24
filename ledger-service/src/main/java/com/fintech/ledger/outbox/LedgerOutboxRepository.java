package com.fintech.ledger.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerOutboxRepository extends JpaRepository<LedgerOutboxEvent, UUID> {

    @Query(
            value = """
            SELECT * FROM ledger_outbox
            WHERE (status = 'PENDING'
            OR (status = 'RETRY' AND retry_at <= NOW()))
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    public List<LedgerOutboxEvent> fetchProcessableBatch(@Param("batchSize") int batchSize);

    @Modifying
    @Query(
            value = """
            UPDATE ledger_outbox
            SET status = :status,
            processed_at = :processed_at
            WHERE id = :id
            """,
            nativeQuery = true
    )
    public void updateProcessedStatus(@Param("id") UUID id, @Param("status") String status
            , @Param("processed_at") Instant processedAt);

    @Modifying
    @Query(
            value = """
            UPDATE ledger_outbox
            SET status = :status,
            retry_count = retry_count+1,
            processed_at = :processed_at
            retry_at = :retry_at
            WHERE id = :id
            """,
            nativeQuery = true
    )
    public void updateRetryStatus(@Param("id") UUID id, @Param("status") String status
            , @Param("retry_at") Instant retryAt, @Param("processed_at") Instant processedAt);
}
