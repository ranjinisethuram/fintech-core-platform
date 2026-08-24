package com.fintech.fraud.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum FraudErrorCode implements ErrorCode {
    FRAUD_EVALUATION_FAILED("FRAUD_001", "Fraud evaluation failed due to internal error.", ErrorType.TECHNICAL, true),
    INVALID_TRANSACTION_DATA("FRAUD_002", "Invalid transaction data provided.", ErrorType.BUSINESS, false),
    INSUFFICIENT_HISTORICAL_DATA("FRAUD_003", "Insufficient historical data for fraud evaluation.", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    FraudErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
