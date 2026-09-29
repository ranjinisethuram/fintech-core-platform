package com.fintech.customer.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum CustomerErrorCode implements ErrorCode {

    CUSTOMER_NOT_FOUND("CUSTOMER_001","Customer not found", ErrorType.BUSINESS, false),
    DUPLICATE_CUSTOMER("CUSTOMER_002","Customer already exist", ErrorType.BUSINESS, false),
    BENEFICIARY_NOT_FOUND("CUSTOMER_003","Beneficiary not found", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    CustomerErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
