package com.fintech.aitool.controller;

import com.fintech.aitool.dto.WalletBalanceResponse;
import com.fintech.aitool.service.AccountToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ai-tools/accounts")
public class AccountToolController {

    private final AccountToolService accountToolService;

    public AccountToolController(AccountToolService accountToolService) {
        this.accountToolService = accountToolService;
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_wallet:fetch')")
    public ResponseEntity<WalletBalanceResponse> getAccountBalance(@PathVariable("id") UUID accountId){
        WalletBalanceResponse resp = this.accountToolService.getAccountBalance(accountId);
        return ResponseEntity.ok(resp);
    }
}
