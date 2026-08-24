package com.fintech.security.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum AuthErrorCode implements ErrorCode {
    UNAUTHORIZED("AUTH_001","Missing token.", ErrorType.BUSINESS, false),
    INVALID_TOKEN("AUTH_002","Invalid or expired token.", ErrorType.BUSINESS, false),
    ACCESS_DENIED("AUTH_003","Access Denied.", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    AuthErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
