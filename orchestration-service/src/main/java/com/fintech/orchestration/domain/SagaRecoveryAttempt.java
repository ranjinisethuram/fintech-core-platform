package com.fintech.orchestration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="saga_recovery_attempt")
public class SagaRecoveryAttempt {

    @Id
    @Column(name="recovery_id", nullable = false, updatable = false)
    private UUID recoveryId;
    @Column(name="saga_id", nullable = false, updatable = false)
    private UUID sagaId;
    @Column(name="attempt_number", nullable = false, updatable = false)
    private int attemptNumber;
    @Column(name="attempt_at", nullable = false, updatable = false)
    private Instant attemptedAt;

    protected SagaRecoveryAttempt(){

    }

    public SagaRecoveryAttempt(UUID sagaId, int attemptNumber){
        this.recoveryId = UUID.randomUUID();
        this.sagaId = sagaId;
        this.attemptNumber = attemptNumber;
        this.attemptedAt = Instant.now();
    }

    public UUID getRecoveryId() {
        return recoveryId;
    }

    public UUID getSagaId() {
        return sagaId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }
}
