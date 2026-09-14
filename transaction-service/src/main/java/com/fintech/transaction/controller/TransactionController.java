package com.fintech.transaction.controller;

import com.fintech.transaction.dto.TransactionRequest;
import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value="/deposit")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:deposit')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest transactionRequest,
                                                       @RequestHeader("Customer-Id") UUID customerId,
                                                       @RequestHeader("Reference-Id") String externalReferenceId){
        TransactionResponse transactionResponse = this.transactionService.depositAmount(transactionRequest,customerId,externalReferenceId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionResponse);
    }

    @PostMapping(value="/withdraw")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:withdraw')")
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody TransactionRequest transactionRequest,
                                                       @RequestHeader("Customer-Id") UUID customerId,
                                                       @RequestHeader("Reference-Id") String externalReferenceId){
        TransactionResponse transactionResponse = this.transactionService.withdrawAmount(transactionRequest,customerId,externalReferenceId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionResponse);
    }

    @PostMapping(value="/transfer")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:transfer')")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransactionRequest transactionRequest,
                                                          @RequestHeader("Customer-Id") UUID customerId,
                                                          @RequestHeader("Reference-Id") String externalReferenceId){
        TransactionResponse transactionResponse = this.transactionService.transferAmount(transactionRequest,customerId,externalReferenceId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionResponse);
    }

    @GetMapping(value="/{id}")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:get')")
    public ResponseEntity<com.fintech.transaction.dto.TransactionStatusResponse> getTransactionStatus(@PathVariable("id") UUID transactionId){
        com.fintech.transaction.dto.TransactionStatusResponse resp = this.transactionService.getTransactionStatus(transactionId);
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

}
