package com.fintech.orchestration.domain;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga")
public class Saga {

    @Id
    @Column(name = "saga_id", nullable = false, updatable = false)
    private UUID sagaId;
    @Column(name = "correlation_id", nullable = false)
    private String correlationId;
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
//    @Enumerated(EnumType.STRING)
//    @Column(name = "current_state", nullable = false)
//    private SagaState currentState;
//    @Enumerated(EnumType.STRING)
//    @Column(name = "next_state")
//    private SagaState nextState;
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step")
    private StepId currentStep;
    @Column(name = "next_step_index")
    private Integer nextStepIndex;
    @Enumerated(EnumType.STRING)
    @Column(name = "saga_status", nullable = false)
    private SagaStatus sagaStatus;
    @Version
    private Long version;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "retry_count")
    private int retryCount;
    @Column(name = "retry_at")
    private Instant retryAt;
    @Column(name = "timeout_at")
    private Instant timeoutAt;
    @Column(name = "failure_reason")
    private String failureReason;

    protected Saga(){

    }

    private Saga(UUID sagaId,
                 String correlationId,
                 String aggregateId
                 //SagaState initialState,
                 //SagaState nextState)
    ){
        this.sagaId = sagaId;
        this.correlationId = correlationId;
        this.aggregateId = aggregateId;
        //this.currentState = initialState;
        this.sagaStatus = SagaStatus.STARTED;
        this.nextStepIndex = 0;
        //this.nextState = nextState;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.timeoutAt = Instant.now().plusSeconds(120);
    }

    public static Saga start(String correlationId,
                             String aggregateId
                             //SagaState initialState,
                             //SagaState nextState)
    ){
        return new Saga(
                UUID.randomUUID(),
                correlationId,
                aggregateId
        );
    }

    public void moveTo(StepId currentStep) {
        //this.currentState = currentState;
        //this.nextState = nextState;
        this.currentStep = currentStep;
        this.sagaStatus = SagaStatus.IN_PROGRESS;
        this.nextStepIndex+=1;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.sagaStatus = SagaStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String failureReason) {
        this.sagaStatus = SagaStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
    }

    public void markForRetry(Duration backoff){
        this.sagaStatus = SagaStatus.WAITING_RETRY;
        this.retryAt = Instant.now().plus(backoff);
        this.updatedAt = Instant.now();
    }

    public void retry(Duration backoff){
        this.sagaStatus = SagaStatus.RECOVERING;
        this.retryCount++;
        this.retryAt = Instant.now().plus(backoff);
        this.updatedAt = Instant.now();
    }

    public void timeout(){
        this.sagaStatus = SagaStatus.TIMED_OUT;
        this.updatedAt = Instant.now();
    }

    public void compensating(){
        //this.currentState = currentState;
        //this.nextState = nextState;
        this.sagaStatus = SagaStatus.COMPENSATING;
        this.updatedAt = Instant.now();
    }

    public void compensated(){
        this.sagaStatus = SagaStatus.COMPENSATED;
        this.updatedAt = Instant.now();
    }

    public UUID getSagaId() {
        return sagaId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public StepId getCurrentStep() {
        return currentStep;
    }

    public Integer getNextStepIndex() {
        return nextStepIndex;
    }

    public SagaStatus getSagaStatus() {
        return sagaStatus;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getRetryAt() {
        return retryAt;
    }

    public Instant getTimeoutAt() {
        return timeoutAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
