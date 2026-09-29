package com.fintech.account.domain;

import com.fintech.accountcontract.domain.AccountType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name="accounts", uniqueConstraints = {@UniqueConstraint(name="uc_customerId_accountType"
,columnNames = {"customer_id","account_type"})})
public class Account {

    @Id
    private  UUID id;
    @Column(name = "customer_id", nullable = false, updatable = false)
    private  UUID customerId;
    @Column(name = "account_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Column(name = "account_status", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    @Column(name = "is_default", nullable = false, updatable = true)
    private boolean isDefault;
    @Column(name = "created_at", nullable = false, updatable = false)
    private  Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Account(){

    }

    public Account(UUID accountId, UUID customerId, AccountType accountType, boolean isDefault) {
        this.id = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.isDefault = isDefault;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.accountStatus = AccountStatus.PENDING;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
        this.updatedAt = Instant.now();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
