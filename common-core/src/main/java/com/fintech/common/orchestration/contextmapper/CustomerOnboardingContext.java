package com.fintech.common.orchestration.contextmapper;

import com.fintech.common.domain.Currency;

import java.util.UUID;

public class CustomerOnboardingContext {

    private UUID customerId;
    private UUID accountId;
    private UUID walletId;
    private UUID ledgerAccountId;
    private Currency currency;
    private boolean isDefaultAccount;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    public void setLedgerAccountId(UUID ledgerAccountId) {
        this.ledgerAccountId = ledgerAccountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public boolean isDefaultAccount() {
        return isDefaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        isDefaultAccount = defaultAccount;
    }
}
