package com.fintech.ledger.repository;

import com.fintech.ledger.domain.AccountCode;
import com.fintech.ledger.domain.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {

    LedgerAccount findByAccountCode(AccountCode accountCode);
}
