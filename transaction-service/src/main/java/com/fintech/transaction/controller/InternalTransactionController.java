package com.fintech.transaction.controller;

import com.fintech.transaction.service.TransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/transactions")
public class InternalTransactionController {

    private final TransactionService transactionService;

    public InternalTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
