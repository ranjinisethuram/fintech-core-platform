package com.fintech.wallet.dto;

import com.fintech.common.domain.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletBalance {
    UUID accountId;
    BigDecimal availableBalance;
    Currency currency;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
