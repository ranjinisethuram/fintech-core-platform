package com.fintech.transaction.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum TransactionErrorCode implements ErrorCode {
    SOURCE_ACCOUNT_ID_MISSING("TRANSACTION_001","Source Account Id is required.",ErrorType.BUSINESS,false),
    DESTINATION_ACCOUNT_ID_MISSING("TRANSACTION_002","Destination Account Id is required.",ErrorType.BUSINESS,false),
    ACCOUNT_IDS_MISSING("TRANSACTION_003","Source Account Id and Destination Account Id are missing.",ErrorType.BUSINESS,false),
    DUPLICATE_TRANSACTION("TRANSACTION_004","Transaction already initiated.",ErrorType.BUSINESS,false),
    TRANSACTION_INITIATION_FAILED("TRANSACTION_005","Transaction initiation failed due to internal errors.",ErrorType.TECHNICAL,false),
    SOURCE_ACCOUNT_NOT_FOUND("TRANSACTION_006","Source account not found.",ErrorType.BUSINESS,false),
    DESTINATION_ACCOUNT_NOT_FOUND("TRANSACTION_007","Destination account not found.",ErrorType.BUSINESS,false),
    SOURCE_ACCOUNT_NOT_ACTIVE("TRANSACTION_008","Source account not active.",ErrorType.BUSINESS,false),
    DESTINATION_ACCOUNT_NOT_ACTIVE("TRANSACTION_009","Destination account not active.",ErrorType.BUSINESS,false),
    INVALID_SOURCE_ACCOUNT("TRANSACTION_010","Customer not authorized to access source account.",ErrorType.BUSINESS,false),
    VALIDATION_FAILED("TRANSACTION_011","Transaction aborted due to failed validation!",ErrorType.TECHNICAL,false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    TransactionErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
