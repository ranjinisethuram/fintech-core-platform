package com.fintech.customer.feign.account;

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
public class AccountQueryClientWrapper {

    private final AccountQueryClient client;

    public AccountQueryClientWrapper(AccountQueryClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "accountFetchFallback")
    @Retry(name = "accountServiceRetry")
    @TimeLimiter(name = "accountServiceTL")
    public CompletableFuture<CustomerAccountsResponse> fetchCustomerAccounts(UUID customerId) {

        return CompletableFuture.supplyAsync(() ->
                client.fetchCustomerAccounts(customerId)
        );
    }

    public CompletableFuture<CustomerAccountsResponse> accountFetchFallback(
            UUID customerId, Throwable ex) {
        System.out.println("Exception in accountFetchCallback: "+ex.getMessage());
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR,"Account Service currently unavailable");
    }
}
