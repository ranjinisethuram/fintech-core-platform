package com.fintech.aitool.service;

import com.fintech.aitool.dto.WalletBalanceResponse;
import com.fintech.aitool.feign.wallet.WalletClientWrapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountToolService {

    private final WalletClientWrapper walletClientWrapper;

    public AccountToolService(WalletClientWrapper walletClientWrapper) {
        this.walletClientWrapper = walletClientWrapper;
    }

    public WalletBalanceResponse getAccountBalance(UUID accountId) {
        WalletBalanceResponse resp = this.walletClientWrapper.getBalance(accountId).join();
        if(resp.getAccountType() == null) resp.setAccountType("SAVINGS");
        return resp;
    }
}
