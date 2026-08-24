package com.fintech.outbox;

public enum OutboxStatus {

    PENDING,
    IN_PROGRESS,
    SUCCESS,
    RETRY,
    FAILED
}
