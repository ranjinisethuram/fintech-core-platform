package com.fintech.common.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public class LedgerEntryRequest {

    private LedgerEntryType ledgerEntryType;
    private UUID accountId;

    public LedgerEntryRequest(LedgerEntryType ledgerEntryType,
                              UUID accountId){
        this.ledgerEntryType = ledgerEntryType;
        this.accountId = accountId;
    }

    @JsonProperty("ledgerEntryType")
    public LedgerEntryType getLedgerEntryType() {
        return ledgerEntryType;
    }

    public void setLedgerEntryType(LedgerEntryType ledgerEntryType) {
        this.ledgerEntryType = ledgerEntryType;
    }

    @JsonProperty("accountId")
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }
}
