package com.fintech.transaction.controller;

import com.fintech.transaction.service.TransactionService;
import com.fintech.transactioncontract.dto.TransactionHistory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/transactions")
public class InternalTransactionController {

    private final TransactionService transactionService;

    public InternalTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(value="/fetch/history")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:fetch')")
    public ResponseEntity<List<TransactionHistory>> getTransactionHistory(
            @RequestParam UUID accountId, @RequestParam Instant createdAfter) {
        List<TransactionHistory> transactionHistory =  transactionService
                .fetchTransactionHistory(accountId, createdAfter);
        return ResponseEntity.status(HttpStatus.OK).body(transactionHistory);
    }
}
