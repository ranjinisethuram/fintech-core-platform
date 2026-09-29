package com.fintech.ledger.controller;

import com.fintech.ledger.service.LedgerService;
import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ledgers")
public class InternalLedgerController {

    private final LedgerService ledgerService;

    public InternalLedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping(value = "/validate")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_validate:validate')")
    ResponseEntity<LedgerAccountValidationResponse> validateLedgerAccount(@Valid @RequestBody
                         LedgerAccountValidationRequest ledgerAccountValidationRequest){
        LedgerAccountValidationResponse ledgerAccountValidationResponse =
                this.ledgerService.validateLedgerAccount(ledgerAccountValidationRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ledgerAccountValidationResponse);
    }
}
