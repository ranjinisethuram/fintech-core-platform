package com.fintech.wallet.domain;

import com.fintech.common.domain.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fund_reservation")
public class FundReservation {

    @Id
    @Column(name = "reservation_id", nullable = false, updatable = false)
    private UUID reservationId;
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;
    @Column(name = "transaction_id", unique = true, nullable = false, updatable = false)
    private UUID transactionId;
    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;
    @Column(name = "currency", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Column(name = "reservation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FundReservationStatus fundReservationStatus;
    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FundReservation(){

    }

    public FundReservation (UUID walletId, UUID transactionId,
                            BigDecimal amountToLock, Instant expiresAt){
        this.reservationId = UUID.randomUUID();
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.currency = Currency.INR;
        this.fundReservationStatus = FundReservationStatus.RESERVED;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void changeFundReservationStatus(FundReservationStatus reservationStatus){
        this.fundReservationStatus = reservationStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public FundReservationStatus getFundReservationStatus() {
        return fundReservationStatus;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
