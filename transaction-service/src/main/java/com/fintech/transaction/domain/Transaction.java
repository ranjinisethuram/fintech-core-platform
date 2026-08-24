package com.fintech.transaction.domain;

import com.fintech.common.domain.Currency;

import com.fintech.common.domain.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;
    @Column(name = "source_account_id", updatable = false)
    private UUID sourceAccountId;
    @Column(name = "destination_account_id", updatable = false)
    private UUID destinationAccountId;
    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;
    @Column(name = "currency", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Column(name = "transaction_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Column(name = "transaction_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    @Column(name = "external_reference_id", nullable = false, unique = true, updatable = false)
    private String externalReferenceId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Transaction(){

    }

    public Transaction(UUID customerId, UUID sourceAccountId, UUID destinationAccountId,
                       BigDecimal amount, Currency currency, String externalReferenceId, TransactionType transactionType){
        this.customerId = customerId;
        this.transactionId = UUID.randomUUID();
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.transactionStatus = TransactionStatus.INITIATED;
        this.externalReferenceId = externalReferenceId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void modifyTransactionStatus(TransactionStatus transactionStatus){
        this.transactionStatus = transactionStatus;
        this.updatedAt = Instant.now();
    }
}
