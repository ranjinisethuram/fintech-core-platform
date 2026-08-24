package com.fintech.wallet.exception;

import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorType;

public enum WalletErrorCode implements ErrorCode {
    WALLET_ALREADY_EXISTS("WALLET_001","Wallet already exists for the customer for the given account type.",ErrorType.BUSINESS,false),
    WALLET_NOT_EXISTS("WALLET_002","Wallet does not exist.", ErrorType.BUSINESS, false),
//    WALLET_UNAUTHORIZED_ACCESS("WALLET_003","Customer not authorized to access wallet.", ErrorType.BUSINESS, false),
    WALLET_NOT_ACTIVE("WALLET_003","Wallet is not active.", ErrorType.BUSINESS, false),
    WALLET_INSUFFICIENT_BALANCE("WALLET_004", "Insufficient balance in the wallet", ErrorType.BUSINESS, false),
    WALLET_DUPLICATE_FUND_RESERVATION("WALLET_005", "Duplicate fund reservation found for the given transaction", ErrorType.BUSINESS, false),
    WALLET_FUND_RESERVATION_NOT_FOUND("WALLET_006","Wallet Fund Not Reserved.", ErrorType.BUSINESS, false);

    private final String errorCode;
    private final String errorMessage;
    private final ErrorType errorType;
    private final boolean retryable;

    WalletErrorCode(String errorCode, String errorMessage, ErrorType errorType, boolean retryable) {
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
