package com.fintech.orchestration.domain;

import com.fintech.common.domain.SagaContextType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_context")
public class SagaContext {

    @Id
    @Column(name = "saga_id", nullable = false, updatable = false)
    private UUID sagaId;
    @Enumerated(EnumType.STRING)
    @Column(name = "saga_context_type",nullable = false, updatable = false)
    private SagaContextType sagaContextType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "context_json", nullable = false)
    private String contextJson;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SagaContext(){

    }

    public SagaContext(UUID sagaId, SagaContextType sagaContextType, String contextJson){
        this.sagaId = sagaId;
        this.sagaContextType = sagaContextType;
        this.contextJson = contextJson;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getSagaId() {
        return sagaId;
    }

    public String getContextJson() {
        return contextJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public SagaContextType getSagaContextType() {
        return sagaContextType;
    }

    public void updateSagaContextJson(String contextJson){
        this.contextJson = contextJson;
        this.updatedAt = Instant.now();
    }
}
