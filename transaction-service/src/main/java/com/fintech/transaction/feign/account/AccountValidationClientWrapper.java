package com.fintech.transaction.feign.account;

import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AccountValidationClientWrapper {

    private final AccountValidationClient client;

    public AccountValidationClientWrapper(AccountValidationClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "accountValidationFallback")
    @Retry(name = "accountServiceRetry")
    @TimeLimiter(name = "accountServiceTL")
    public CompletableFuture<AccountValidationResponse> validateAccount(AccountValidationRequest accountValidationRequest) {

        return CompletableFuture.supplyAsync(() ->
                client.validateAccount(accountValidationRequest)
        );
    }

    public CompletableFuture<AccountValidationResponse> accountValidationFallback(
            AccountValidationRequest accountValidationRequest, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR,"Account Service currently unavailable");
    }

}
