package com.fintech.customer.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @Column(name = "beneficiary_id")
    private UUID beneficiaryId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Beneficiary() {}

    public Beneficiary(UUID customerId, String name, String accountId) {
        this.beneficiaryId = UUID.randomUUID();
        this.customerId = customerId;
        this.name = name;
        this.accountId = accountId;
        this.createdAt = Instant.now();
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getAccountId() {
        return accountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
