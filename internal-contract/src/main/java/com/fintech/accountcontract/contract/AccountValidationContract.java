package com.fintech.accountcontract.contract;

import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;

public interface AccountValidationContract {

    AccountValidationResponse validateAccount(AccountValidationRequest accountValidationRequest);
}
