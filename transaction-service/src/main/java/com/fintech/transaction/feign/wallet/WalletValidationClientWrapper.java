package com.fintech.transaction.feign.wallet;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class WalletValidationClientWrapper {

    private final WalletValidationClient walletClient;

    public WalletValidationClientWrapper(WalletValidationClient walletClient) {
        this.walletClient = walletClient;
    }

    @CircuitBreaker(name = "walletServiceCB", fallbackMethod = "fallback")
    @Retry(name = "walletServiceRetry")
    @TimeLimiter(name = "walletServiceTL")
    public CompletableFuture<WalletAccountValidationResponse> validateAccount(
            WalletAccountValidationRequest walletAccountValidationRequest) {

        return CompletableFuture.supplyAsync(() ->
                walletClient.validateWallet(walletAccountValidationRequest)
        );
    }

    public CompletableFuture<WalletAccountValidationResponse> fallback(
            String accountId, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR,"Wallet Service currently unavailable");
    }
}
