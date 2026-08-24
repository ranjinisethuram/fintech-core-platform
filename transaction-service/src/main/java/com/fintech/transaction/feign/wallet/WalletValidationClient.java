package com.fintech.transaction.feign.wallet;

import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;
import com.fintech.transaction.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "wallet-service",
        url = "${clients.wallet-service.url}",
        configuration = FeignClientConfig.class
)
public interface WalletValidationClient {

    @PostMapping("/internal/wallets/validate")
    WalletAccountValidationResponse validateWallet(@RequestBody WalletAccountValidationRequest walletAccountValidationRequest);
}
