package com.fintech.customer.controller;

import com.fintech.customer.dto.CreateCustomerRequest;
import com.fintech.customer.dto.CustomerProfile;
import com.fintech.customer.dto.CustomerStatusResponse;
import com.fintech.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerContoller {

    private final CustomerService customerService;

    public CustomerContoller(CustomerService customerService){
        this.customerService = customerService;
    }

    @PostMapping(value="/create")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_customer:create')")
    public ResponseEntity<CustomerStatusResponse> create(
            @Valid @RequestBody CreateCustomerRequest createCustomerRequest){
        CustomerStatusResponse customerResponse = this.customerService
                .createCustomer(createCustomerRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(customerResponse);
    }

    @GetMapping(value="/status/{customerId}")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_customer:get')")
    public ResponseEntity<CustomerStatusResponse> fetchCustomerStatus(@PathVariable UUID customerId){
        CustomerStatusResponse customerResponse = this.customerService.fetchCustomerStatus(customerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(customerResponse);
    }

    @GetMapping(value="/profile/{customerId}")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_customer:get')")
    public ResponseEntity<CustomerProfile> fetchCustomerProfile(@PathVariable UUID customerId){
        CustomerProfile customerProfile = this.customerService.getCustomerProfile(customerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(customerProfile);
    }

}
