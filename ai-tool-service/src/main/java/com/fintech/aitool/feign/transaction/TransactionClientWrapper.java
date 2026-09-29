package com.fintech.aitool.feign.transaction;

import com.fintech.aitool.dto.TransactionStatusResponse;
import com.fintech.aitool.feign.TransactionClient;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.transactioncontract.dto.TransactionHistory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionClientWrapper {

    private final TransactionClient transactionClient;

    public TransactionClientWrapper(TransactionClient transactionClient) {
        this.transactionClient = transactionClient;
    }

    @CircuitBreaker(name = "transactionServiceCB", fallbackMethod = "transactionHistoryFallback")
    @Retry(name = "transactionServiceRetry")
    @TimeLimiter(name = "transactionServiceTL")
    public CompletableFuture<List<TransactionHistory>> fetchTransactionHistory(UUID accountId, Instant createdAfter) {
        return CompletableFuture.supplyAsync(() -> transactionClient.fetchTransactionHistory(accountId, createdAfter));
    }

    @CircuitBreaker(name = "transactionServiceCB", fallbackMethod = "transactionStatusFallback")
    @Retry(name = "transactionServiceRetry")
    @TimeLimiter(name = "transactionServiceTL")
    public CompletableFuture<TransactionStatusResponse> getTransactionStatus(UUID transactionId) {
        return CompletableFuture.supplyAsync(() -> transactionClient.getTransactionStatus(transactionId));
    }

    public CompletableFuture<List<TransactionHistory>> transactionHistoryFallback(UUID accountId, Instant createdAfter, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR, "Transaction Service currently unavailable");
    }

    public CompletableFuture<TransactionStatusResponse> transactionStatusFallback(UUID transactionId, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR, "Transaction Service currently unavailable");
    }
}
