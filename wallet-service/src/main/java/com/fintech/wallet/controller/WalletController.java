package com.fintech.wallet.controller;

import com.fintech.wallet.dto.WalletBalance;
import com.fintech.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping(value = "/balance")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_wallet:fetch')")
    public ResponseEntity<WalletBalance> getAccountBalance(@RequestParam("accountId") UUID accountId){
        WalletBalance walletBalance = this.walletService.getAvailableBalance(accountId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(walletBalance);
    }
}
