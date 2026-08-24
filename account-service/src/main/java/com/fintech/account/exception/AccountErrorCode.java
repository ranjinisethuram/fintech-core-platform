package com.fintech.account.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum AccountErrorCode implements ErrorCode {
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_001","Account already exists for the customer for the given account type.",ErrorType.BUSINESS,false),
    ACCOUNT_NOT_EXISTS("ACCOUNT_002","Account does not exist.", ErrorType.BUSINESS, false),
    ACCOUNT_ILLEGAL_ACCESS("ACCOUNT_003","Customer not authorized to access the account.", ErrorType.BUSINESS, false),
    ACCOUNT_NOT_ACTIVE("ACCOUNT_004","Account is not active.", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    AccountErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
