package com.fintech.common.exception;

public interface ErrorCode {

    String getErrorCode();
    String getErrorMessage();
    ErrorType getErrorType();
    boolean isRetryable();
}
