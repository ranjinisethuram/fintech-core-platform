package com.fintech.aitool.feign;

import com.fintech.aitool.dto.WalletBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "wallet-service", url = "${clients.wallet-service.url}", configuration = FeignClientConfig.class)
public interface WalletClient {

    @GetMapping("/wallet/balance")
    WalletBalanceResponse getBalance(@RequestParam("accountId") UUID accountId);
}
