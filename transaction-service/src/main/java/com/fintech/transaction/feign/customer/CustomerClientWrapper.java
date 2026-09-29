package com.fintech.transaction.feign.customer;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.customercontract.dto.CustomerProfile;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerClientWrapper {

    private final CustomerClient client;

    public CustomerClientWrapper(CustomerClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "customerServiceCB", fallbackMethod = "customerServiceFallback")
    @Retry(name = "customerServiceRetry")
    @TimeLimiter(name = "customerServiceTL")
    public CompletableFuture<CustomerProfile> fetchCustomerProfile(UUID customerId) {
        return CompletableFuture.supplyAsync(() ->
                client.fetchCustomerProfile(customerId)
        );
    }

    public CompletableFuture<CustomerProfile> customerServiceFallback(UUID customerId, Throwable ex) {
        throw new BaseException(CommonErrorCode.INTERNAL_ERROR, "Customer Service currently unavailable");
    }
}
