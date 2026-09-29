package com.fintech.transaction.feign.customer;

import com.fintech.customercontract.dto.CustomerProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "customer-service",
        url = "${clients.customer-service.url}",
        configuration = com.fintech.transaction.feign.FeignClientConfig.class
)
public interface CustomerClient {

    @GetMapping("/customers/profile/{customerId}")
    CustomerProfile fetchCustomerProfile(@PathVariable UUID customerId);
}
