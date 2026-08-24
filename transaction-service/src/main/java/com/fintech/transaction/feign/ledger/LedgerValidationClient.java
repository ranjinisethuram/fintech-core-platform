package com.fintech.transaction.feign.ledger;

import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;
import com.fintech.transaction.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "ledger-service",
        url = "${clients.ledger-service.url}",
        configuration = FeignClientConfig.class
)
public interface LedgerValidationClient {

    @PostMapping("/internal/ledger/validate")
    LedgerAccountValidationResponse validateLedger(
            @RequestBody LedgerAccountValidationRequest ledgerAccountValidationRequest);
}
