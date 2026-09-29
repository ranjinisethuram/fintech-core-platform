package com.fintech.aitool.controller;

import com.fintech.aitool.dto.TransactionListResponse;
import com.fintech.aitool.dto.TransactionStatusResponse;
import com.fintech.aitool.service.TransactionToolService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/ai-tools/transactions")
public class TransactionToolController {

    private final TransactionToolService transactionToolService;

    public TransactionToolController(TransactionToolService transactionToolService) {
        this.transactionToolService = transactionToolService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:fetch')")
    public ResponseEntity<TransactionListResponse> getTransactionHistory(
            @RequestParam("accountId") UUID accountId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "limit", required = false) Integer limit
    ){
        Instant start = from == null ? Instant.EPOCH : from;
        TransactionListResponse resp = this.transactionToolService.getTransactionHistory(accountId, start, to, limit);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_transaction:get')")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(@PathVariable("id") UUID transactionId){
        TransactionStatusResponse resp = this.transactionToolService.getTransactionStatus(transactionId);
        return ResponseEntity.ok(resp);
    }
}
