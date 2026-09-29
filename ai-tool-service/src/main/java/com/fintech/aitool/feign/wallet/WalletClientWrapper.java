package com.fintech.aitool.feign.wallet;

import com.fintech.aitool.dto.WalletBalanceResponse;
import com.fintech.aitool.feign.WalletClient;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class WalletClientWrapper {

    private final WalletClient walletClient;

    public WalletClientWrapper(WalletClient walletClient) {
        this.walletClient = walletClient;
    }

    @CircuitBreaker(name = "walletServiceCB", fallbackMethod = "walletBalanceFallback")
    @Retry(name = "walletServiceRetry")
    @TimeLimiter(name = "walletServiceTL")
    public CompletableFuture<WalletBalanceResponse> getBalance(UUID accountId) {
        return CompletableFuture.supplyAsync(() -> walletClient.getBalance(accountId));
    }

    public CompletableFuture<WalletBalanceResponse> walletBalanceFallback(UUID accountId, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR, "Wallet Service currently unavailable");
    }
}
