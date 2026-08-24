package com.fintech.common.exception;

public enum CommonErrorCode implements ErrorCode{

    VALIDATION_FAILED("COMMON_001","Request validation failed", ErrorType.BUSINESS, false),
    DB_QUERY_TIMEOUT("COMMON_002","Database query timeout", ErrorType.TRANSIENT, true),
    DB_CONNECTION_POOL_TIMEOUT("COMMON_003","Database connection pool timeout", ErrorType.TRANSIENT, true),
    SERVICE_UNAVAILABLE("COMMON_004","Service is currently unavailable", ErrorType.TRANSIENT, true),
    INTERNAL_ERROR("COMMON_005","Unexpected error occurred", ErrorType.TECHNICAL, false),
    JSON_DESERIALIZATION_ERROR("COMMON_006","Error while deserializing json string",ErrorType.TECHNICAL,false),
    JSON_SERIALIZATION_ERROR("COMMON_007","Error while serializing object",ErrorType.TECHNICAL,false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    CommonErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
