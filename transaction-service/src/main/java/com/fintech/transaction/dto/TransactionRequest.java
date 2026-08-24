package com.fintech.transaction.dto;

import com.fintech.common.domain.Currency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionRequest {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Format must be up to 10 digits with 2 decimals")
    private BigDecimal amount;
    @NotNull(message = "Currency is required.")
    private Currency currency;


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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
