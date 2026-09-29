package com.fintech.orchestration.feign.transaction;

import com.fintech.orchestration.feign.FeignClientConfig;
import com.fintech.transactioncontract.dto.TransactionHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "transaction-service",
        url = "${clients.transaction-service.url}",
        configuration = FeignClientConfig.class
)
public interface TransactionQueryClient {

    @GetMapping(value="/internal/transactions/fetch/history")
    List<TransactionHistory> fetchTransactionHistory(@RequestParam UUID accountId,
                                                     @RequestParam Instant createAfter);
}
