package com.fintech.wallet.repository;

import com.fintech.wallet.domain.FundReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FundReservationRepository extends JpaRepository<FundReservation, UUID> {

    FundReservation findByWalletIdAndTransactionId(UUID walletId, UUID transactionId);
}
