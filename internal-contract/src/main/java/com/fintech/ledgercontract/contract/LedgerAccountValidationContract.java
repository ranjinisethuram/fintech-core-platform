package com.fintech.ledgercontract.contract;

import com.fintech.ledgercontract.dto.LedgerAccountValidationRequest;
import com.fintech.ledgercontract.dto.LedgerAccountValidationResponse;

public interface LedgerAccountValidationContract {

    LedgerAccountValidationResponse validateLedgerAccount(LedgerAccountValidationRequest ledgerAccountValidationRequest);
}
