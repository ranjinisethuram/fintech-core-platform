package com.fintech.ledger.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum LedgerErrorCode implements ErrorCode {
    LEDGER_ACCOUNT_ALREADY_EXISTS("LEDGER_001","Ledger account already exists for the customer for the given account type.",ErrorType.BUSINESS,false),
    LEDGER_ENTRY_ALREADY_EXISTS("LEDGER_002","Ledger entry already exists for the given transaction.", ErrorType.BUSINESS, false),
    LEDGER_ACCOUNT_UNAUTHORIZED("LEDGER_003","Customer not authorized to access ledger account.", ErrorType.BUSINESS, false),
    LEDGER_ACCOUNT_NOT_EXISTS("LEDGER_004","Ledger account does not exist.", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    LedgerErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
