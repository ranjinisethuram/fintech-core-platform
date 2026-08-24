package com.fintech.ledger.domain;

import com.fintech.common.domain.Currency;
import com.fintech.common.domain.LedgerEntryType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ledger",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "transactionId_account_entryType",
            columnNames = {"transaction_id","ledger_account_id","ledger_entry_type"}
        )
    }
)
public class Ledger {
    @Id
    @Column(name = "entry_id", nullable = false, updatable = false)
    UUID entryId;
    @Column(name = "transaction_id", nullable = false, updatable = false)
    UUID transactionId;
    @Column(name = "ledger_account_id", nullable = false, updatable = false)
    UUID ledgerAccountId;
    @Column(name = "ledger_entry_type", nullable = false, updatable = false)
    String ledgerEntryType;
    @Column(name = "credit_amount", nullable = false, updatable = false)
    BigDecimal creditAmount;
    @Column(name = "debit_amount", nullable = false, updatable = false)
    BigDecimal debitAmount;
    @Column(name = "currency", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    Currency currency;
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    protected Ledger(){

    }

    public Ledger(UUID transactionId, UUID ledgerAccountId
            , BigDecimal amount, Currency currency, boolean isCredit){
        this.entryId = UUID.randomUUID();
        this.transactionId = transactionId;
        this.ledgerAccountId = ledgerAccountId;
        this.createdAt = Instant.now();
        if(isCredit){
            this.ledgerEntryType = LedgerEntryType.CREDIT.name();
            this.creditAmount = amount;
            this.debitAmount = BigDecimal.ZERO;
        }else{
            this.ledgerEntryType = LedgerEntryType.DEBIT.name();
            this.debitAmount = amount;
            this.creditAmount = BigDecimal.ZERO;
        }
        this.currency = currency;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Currency getCurrency() {
        return currency;
    }
}
