package com.fintech.orchestration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "saga_error")
public class SagaError {

    @Id
    @Column(name = "saga_id", nullable = false, updatable = false)
    private String sagaId;
    @Column(name = "message_id", nullable = false, updatable = false)
    private String messageId;
    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private String aggregateId;
    @Column(name = "aggregate_type", nullable = false, updatable = false)
    private String aggregateType;
    @Column(name = "correlation_id", nullable = false, updatable = false)
    private String correlationId;
    @Column(name = "payload", nullable = false, updatable = false)
    private String payload;
    @Column(name = "reason", nullable = false, updatable = false)
    private String reason;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SagaError(){

    }

    public SagaError (String sagaId, String messageId, String aggregateId, String aggregateType,
                      String correlationId, String payload, String reason){
        this.sagaId = sagaId;
        this.messageId = messageId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.correlationId = correlationId;
        this.payload = payload;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getPayload() {
        return payload;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
