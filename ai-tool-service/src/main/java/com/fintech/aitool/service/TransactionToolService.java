package com.fintech.aitool.service;

import com.fintech.aitool.dto.TransactionListResponse;
import com.fintech.aitool.dto.TransactionStatusResponse;
import com.fintech.aitool.feign.transaction.TransactionClientWrapper;
import com.fintech.transactioncontract.dto.TransactionHistory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionToolService {

    private final TransactionClientWrapper transactionClientWrapper;

    public TransactionToolService(TransactionClientWrapper transactionClientWrapper) {
        this.transactionClientWrapper = transactionClientWrapper;
    }

    public TransactionListResponse getTransactionHistory(UUID accountId, Instant from, Instant to, Integer limit){
        List<TransactionHistory> history = this.transactionClientWrapper.fetchTransactionHistory(accountId, from).join();
        // map to response and apply limit and filter by to
        List<TransactionListResponse.TransactionItem> items = history.stream()
                .filter(h -> to == null || !h.getTimestamp().isAfter(to))
                .limit(limit == null ? Long.MAX_VALUE : limit)
                .map(h -> {
                    TransactionListResponse.TransactionItem it = new TransactionListResponse.TransactionItem();
                    it.setTransactionId(h.getTransactionId().toString());
                    it.setType(h.getTransactionType().name());
                    it.setAmount(h.getAmount());
                    it.setCurrency(h.getCurrency().name());
                    it.setStatus(h.getStatus());
                    it.setTimestamp(h.getTimestamp());
                    return it;
                }).collect(Collectors.toList());
        TransactionListResponse resp = new TransactionListResponse();
        resp.setTransactions(items);
        return resp;
    }

    public TransactionStatusResponse getTransactionStatus(UUID transactionId){
        return this.transactionClientWrapper.getTransactionStatus(transactionId).join();
    }
}
