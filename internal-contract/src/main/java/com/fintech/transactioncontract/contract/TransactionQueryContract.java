package com.fintech.transactioncontract.contract;

import com.fintech.transactioncontract.dto.TransactionHistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionQueryContract {
    List<TransactionHistory> fetchTransactionHistory(UUID accountId, Instant createdAfter);
}
