package com.fintech.customer.feign.account;

import com.fintech.accountcontract.contract.AccountQueryContract;
import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountQueryAdapter implements AccountQueryContract {
    private final AccountQueryClientWrapper wrapper;

    public AccountQueryAdapter(AccountQueryClientWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public CustomerAccountsResponse fetchCustomerAccountDetails(UUID customerId) {
        return wrapper.fetchCustomerAccounts(customerId).join();
    }
}
