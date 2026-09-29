package com.fintech.orchestration.feign.transaction;

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
public class TransactionQueryClientWrapper {

    private final TransactionQueryClient transactionQueryClient;

    public TransactionQueryClientWrapper(TransactionQueryClient transactionQueryClient) {
        this.transactionQueryClient = transactionQueryClient;
    }

    @CircuitBreaker(name = "transactionServiceCB", fallbackMethod = "transactionQueryFallback")
    @Retry(name = "transactionServiceRetry")
    @TimeLimiter(name = "transactionServiceTL")
    public CompletableFuture<List<TransactionHistory>> fetchTransactionHistory(UUID accountId,
                               Instant createdAfter) {
        return CompletableFuture.supplyAsync(() ->
                transactionQueryClient.fetchTransactionHistory(accountId, createdAfter)
        );
    }

    public CompletableFuture<List<TransactionHistory>> transactionQueryFallback(UUID accountId,
                                        Instant createdAfter, Throwable throwable) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR, "TransactionService currently not available.");
    }
}
