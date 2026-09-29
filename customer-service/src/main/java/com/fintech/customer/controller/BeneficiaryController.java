package com.fintech.customer.controller;

import com.fintech.customer.dto.BeneficiaryRequest;
import com.fintech.customer.dto.BeneficiaryResponse;
import com.fintech.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customers/{customerId}/beneficiaries")
public class BeneficiaryController {

    private final CustomerService customerService;

    public BeneficiaryController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_customer:modify')")
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(@PathVariable UUID customerId,
                                                              @RequestBody BeneficiaryRequest request){
        BeneficiaryResponse response = this.customerService.addBeneficiary(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = "{beneficiaryId}")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_customer:modify')")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable UUID customerId,
                                                  @PathVariable UUID beneficiaryId){
        this.customerService.deleteBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}
