package com.fintech.orchestration.feign.transaction;

import com.fintech.transactioncontract.contract.TransactionQueryContract;
import com.fintech.transactioncontract.dto.TransactionHistory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionQueryAdapter implements TransactionQueryContract {

    private final TransactionQueryClientWrapper transactionQueryClientWrapper;

    public TransactionQueryAdapter(TransactionQueryClientWrapper transactionQueryClientWrapper) {
        this.transactionQueryClientWrapper = transactionQueryClientWrapper;
    }

    @Override
    public List<TransactionHistory> fetchTransactionHistory(UUID accountId, Instant createdAfter) {
        return transactionQueryClientWrapper.fetchTransactionHistory(
                accountId, createdAfter).join();
    }
}
