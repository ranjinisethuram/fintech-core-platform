package com.fintech.outbox;

import com.fintech.common.messaging.MessageEnvelope;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @Column(name  ="aggregate_type", nullable = false)
    protected String aggregateType;

    @Column(name  ="aggregate_id", nullable = false)
    protected String aggregateId;

    @Column(name  ="event_type", nullable = false)
    protected String eventType;

    @Column(name = "saga_id")
    protected String sagaId;
    @Column(name = "correlation_id")
    protected String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "payload", nullable = false)
    protected MessageEnvelope<?> payload;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    protected OutboxStatus status;

    @Column(name = "retry_count")
    protected int retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "retry_at")
    protected Instant retryAt;

    @Column(name = "processed_at")
    protected Instant processedAt;

    @Version
    protected Integer version;

    public UUID getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public MessageEnvelope<?> getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getRetryAt() {
        return retryAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setPayload(MessageEnvelope<?> payload) {
        this.payload = payload;
    }

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setRetryAt(Instant retryAt) {
        this.retryAt = retryAt;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void markProcessed() {
        this.status = OutboxStatus.SUCCESS;
        this.processedAt = Instant.now();
    }

    public void markInprogress() {
        this.status = OutboxStatus.IN_PROGRESS;
        this.processedAt = Instant.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = Instant.now();
    }

    public void markRetry(Duration backoff){
        this.status = OutboxStatus.RETRY;
        this.retryCount++;
        this.retryAt = Instant.now().plus(backoff);
        this.processedAt = Instant.now();
    }
}
