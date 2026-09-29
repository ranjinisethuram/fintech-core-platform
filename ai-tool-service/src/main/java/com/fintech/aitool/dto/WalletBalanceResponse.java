package com.fintech.aitool.dto;

import java.math.BigDecimal;

public class WalletBalanceResponse {
    private String accountId;
    private String accountType;
    private BigDecimal available_balance;
    private String currency;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getAvailable_balance() {
        return available_balance;
    }

    public void setAvailable_balance(BigDecimal available_balance) {
        this.available_balance = available_balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
