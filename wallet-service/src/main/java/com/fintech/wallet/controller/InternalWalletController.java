package com.fintech.wallet.controller;

import com.fintech.wallet.service.WalletService;
import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/wallets")
public class InternalWalletController {

    private final WalletService walletService;

    public InternalWalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping(value = "/validate")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_wallet:validate')")
    ResponseEntity<WalletAccountValidationResponse> validateWalletAccount(@Valid @RequestBody
                                           WalletAccountValidationRequest walletAccountValidationRequest){
        WalletAccountValidationResponse walletAccountValidationResponse = this.walletService
                .validateWalletAccount(walletAccountValidationRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(walletAccountValidationResponse);
    }
}
