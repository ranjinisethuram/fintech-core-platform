package com.fintech.walletcontract.dto;

import com.fintech.common.domain.TransactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class WalletAccountValidationRequest {
    @NotNull(message = "Account Id cannot be null.")
    UUID accountId;
    @NotNull(message = "TransactionType cannot be null.")
    TransactionType transactionType;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
