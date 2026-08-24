package com.fintech.transaction.feign.account;

import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.transaction.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "account-service",
        url = "${clients.account-service.url}",
        configuration = FeignClientConfig.class
)
public interface AccountValidationClient {

    @PostMapping("/internal/accounts/validate")
    AccountValidationResponse validateAccount( @RequestBody AccountValidationRequest accountValidationRequest);
}
