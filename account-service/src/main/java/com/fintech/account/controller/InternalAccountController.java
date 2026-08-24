package com.fintech.account.controller;

import com.fintech.account.service.AccountService;
import com.fintech.accountcontract.dto.AccountSummary;
import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ("/internal/accounts")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(value = "/validate")
    public ResponseEntity<AccountValidationResponse> validate(@Valid @RequestBody AccountValidationRequest
                                                              accountValidationRequest){
        AccountValidationResponse accountValidationResponse = this.accountService.validateAccount
                (accountValidationRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(accountValidationResponse);
    }

    @PostMapping(value = "/fetch")
    public ResponseEntity<CustomerAccountsResponse> fetchCustomerAccountDetails(@RequestParam UUID customerId){
        List<AccountSummary> customerAccounts = this.accountService.fetchCustomerAccountDetails(customerId);
        CustomerAccountsResponse customerAccountsResponse = new CustomerAccountsResponse();
        customerAccountsResponse.setCustomerId(customerId.toString());
        customerAccountsResponse.setCustomerAccounts(customerAccounts);
        return ResponseEntity.status(HttpStatus.OK)
                .body(customerAccountsResponse);
    }
}
