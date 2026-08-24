package com.fintech.ledger.domain;

import com.fintech.common.domain.Currency;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_account")
public class LedgerAccount {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;
    @Column(name = "account_code", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    AccountCode accountCode;
    @Column(name = "currency", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    Currency currency;
    @Column(name = "wallet_id", updatable = false)
    UUID walletId;
    @Column(name = "status", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    LedgerStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    protected LedgerAccount(){

    }

    public LedgerAccount(UUID ledgerAccountId, AccountCode accountCode, Currency currency, LedgerStatus ledgerStatus){
        this.id = ledgerAccountId;
        this.accountCode = accountCode;
        this.currency = currency;
        this.status = ledgerStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public LedgerAccount(UUID ledgerAccountId, AccountCode accountCode, Currency currency, UUID walletId){
        this.id = ledgerAccountId;
        this.accountCode = accountCode;
        this.currency = currency;
        this.walletId = walletId;
        this.status = LedgerStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public AccountCode getAccountCode() {
        return accountCode;
    }

    public Currency getCurrency() {
        return currency;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public LedgerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void changeStatus(LedgerStatus status){
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
