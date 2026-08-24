package com.fintech.transaction.feign.ledger;

import com.fintech.ledgercontract.contract.LedgerAccountValidationContract;
import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;
import org.springframework.stereotype.Component;

@Component
public class LedgerValidationAdapter implements LedgerAccountValidationContract {
    private final LedgerValidationClientWrapper wrapper;

    public LedgerValidationAdapter(LedgerValidationClientWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public LedgerAccountValidationResponse validateLedgerAccount(LedgerAccountValidationRequest ledgerAccountValidationRequest) {
        return wrapper.validateAccount(ledgerAccountValidationRequest).join();
    }
}
