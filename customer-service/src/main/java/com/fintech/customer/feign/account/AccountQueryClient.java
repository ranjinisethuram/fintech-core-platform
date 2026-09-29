package com.fintech.customer.feign.account;

import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import com.fintech.customer.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "account-service",
        url = "${clients.account-service.url}",
        configuration = FeignClientConfig.class
)
public interface AccountQueryClient {

    @PostMapping(value = "/internal/accounts/fetch")
    CustomerAccountsResponse fetchCustomerAccounts(@RequestParam UUID customerId);
}
