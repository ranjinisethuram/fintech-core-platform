package com.fintech.orchestration.domain;

public enum SagaStatus {
    STARTED,
    IN_PROGRESS,
    WAITING_RETRY,
    RECOVERING,
    TIMED_OUT,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED
}
