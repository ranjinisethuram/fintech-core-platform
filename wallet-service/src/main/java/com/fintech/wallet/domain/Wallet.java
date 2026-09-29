package com.fintech.wallet.domain;

import com.fintech.common.domain.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;
    @Column(name = "account_id", nullable = false, updatable = false, unique = true)
    private UUID accountId;
    @Column(name = "currency", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;
    @Column(name = "locked_balance", nullable = false)
    private BigDecimal lockedBalance;
    @Column(name = "wallet_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private WalletStatus walletStatus;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    protected Wallet(){

    }

    public Wallet(UUID walletId, UUID accountId){
        this.walletId = walletId;
        this.accountId = accountId;
        this.currency = Currency.INR;
        this.availableBalance = BigDecimal.ZERO;
        this.lockedBalance = BigDecimal.ZERO;
        this.walletStatus = WalletStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void creditAmount(BigDecimal amount){
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = Instant.now();
    }

    public void releaseLockedAmount(BigDecimal amount){
        this.lockedBalance = this.lockedBalance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    public void compensateAvailableBalance(BigDecimal amount) {
        this.lockedBalance = this.lockedBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = Instant.now();
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getLockedBalance() {
        return lockedBalance;
    }

    public WalletStatus getWalletStatus() {
        return walletStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void changeWalletStatus(WalletStatus newStatus){
        this.walletStatus = newStatus;
        this.updatedAt = Instant.now();
    }

    public Long getVersion() {
        return version;
    }
}
