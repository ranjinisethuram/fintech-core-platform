package com.fintech.orchestration.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum OrchestrationErrorCode implements ErrorCode {

    ORCH_SAGA_ALREADY_AVAILABLE("ORCH_001","Saga already available!",ErrorType.TECHNICAL,false),
    ORCH_SAGA_NOT_FOUND("ORCH_002","Saga not found.",ErrorType.TECHNICAL,false),
    ORCH_SAGA_CONTEXT_ALREADY_AVAILABLE("ORCH_003","Saga context already available!",ErrorType.TECHNICAL,false),
    ORCH_SAGA_CONTEXT_NOT_FOUND("ORCH_004","Saga Context not found.",ErrorType.TECHNICAL,false),
    ORCH_EVENT_DESERIALIZATION_FAILED("ORCH_005","Could not deserialze the kafka message.", ErrorType.TECHNICAL, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    OrchestrationErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.retryable = retryable;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}
