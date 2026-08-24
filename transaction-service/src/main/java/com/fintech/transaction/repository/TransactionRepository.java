package com.fintech.transaction.repository;

import com.fintech.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

List<Transaction> findBySourceAccountIdAndCreatedAtAfter(
                UUID sourceAccountId,
                Instant createdAtAfter);
}
