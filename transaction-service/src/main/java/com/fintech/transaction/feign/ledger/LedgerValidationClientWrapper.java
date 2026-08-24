package com.fintech.transaction.feign.ledger;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class LedgerValidationClientWrapper {

    private final LedgerValidationClient ledgerClient;

    public LedgerValidationClientWrapper(LedgerValidationClient ledgerClient) {
        this.ledgerClient = ledgerClient;
    }

    @CircuitBreaker(name = "ledgerServiceCB", fallbackMethod = "fallback")
    @Retry(name = "ledgerServiceRetry")
    @TimeLimiter(name = "ledgerServiceTL")
    public CompletableFuture<LedgerAccountValidationResponse> validateAccount(
            LedgerAccountValidationRequest ledgerAccountValidationRequest) {

        return CompletableFuture.supplyAsync(() ->
                ledgerClient.validateLedger(ledgerAccountValidationRequest)
        );
    }

    public CompletableFuture<LedgerAccountValidationResponse> fallback(
            String accountId, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR,"Ledger Service currently unavailable");
    }
}
