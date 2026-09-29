package com.fintech.aitool.feign;

import com.fintech.transactioncontract.dto.TransactionHistory;
import com.fintech.aitool.dto.TransactionStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "transaction-service", url = "${clients.transaction-service.url}", configuration = FeignClientConfig.class)
public interface TransactionClient {

    @GetMapping("/internal/transactions/fetch/history")
    List<TransactionHistory> fetchTransactionHistory(@RequestParam UUID accountId, @RequestParam Instant createdAfter);

    @GetMapping("/transactions/{id}")
    TransactionStatusResponse getTransactionStatus(@PathVariable("id") UUID transactionId);
}
