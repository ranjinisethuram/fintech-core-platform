package com.fintech.fraud.controller;

import com.fintech.fraud.dto.FraudEvaluationRequest;
import com.fintech.fraud.dto.FraudEvaluationResponse;
import com.fintech.fraud.service.FraudService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fraud")
public class FraudController {

    private final FraudService fraudService;

    public FraudController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('USER') and hasAuthority('SCOPE_fraud:evaluate')")
    public ResponseEntity<FraudEvaluationResponse> evaluate(@Valid @RequestBody FraudEvaluationRequest request) {
        return ResponseEntity.ok(fraudService.evaluate(request));
    }
}
