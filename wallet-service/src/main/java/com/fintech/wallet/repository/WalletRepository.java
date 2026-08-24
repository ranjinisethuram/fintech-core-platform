package com.fintech.wallet.repository;

import com.fintech.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Wallet findByAccountId(UUID accountId);

    @Modifying
    @Query(
            value = """
            UPDATE wallet
            SET available_balance = available_balance - :amount,
            locked_balance = locked_balance + :amount,
            updated_at = :updatedAt
            WHERE wallet_id = :walletId
            AND available_balance >= :amount
            """,
            nativeQuery = true
    )
    int reserveFunds(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount,
                         @Param("updatedAt") Instant updatedAt);
}
