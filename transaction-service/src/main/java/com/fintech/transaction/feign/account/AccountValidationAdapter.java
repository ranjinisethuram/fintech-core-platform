package com.fintech.transaction.feign.account;

import com.fintech.accountcontract.contract.AccountValidationContract;
import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountValidationAdapter implements AccountValidationContract {
    private final AccountValidationClientWrapper wrapper;

    public AccountValidationAdapter(AccountValidationClientWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public AccountValidationResponse validateAccount(AccountValidationRequest accountValidationRequest) {
        return wrapper.validateAccount(accountValidationRequest).join();
    }
}
