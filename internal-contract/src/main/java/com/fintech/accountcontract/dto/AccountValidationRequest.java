package com.fintech.accountcontract.dto;

import com.fintech.common.domain.Currency;
import com.fintech.common.domain.TransactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AccountValidationRequest {

    @NotNull(message = "CustomerId is required.")
    private UUID customerId;
    @NotNull(message = "TransactionType is required.")
    private TransactionType transactionType;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(UUID destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
}
