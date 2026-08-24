package com.fintech.ledgercontract.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class LedgerAccountValidationRequest {

    @NotNull(message = "Wallet Id cannot be null.")
    UUID walletId;
    @NotNull(message = "Ledger Account Id cannot be null.")
    UUID ledgerAccountId;

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
}
