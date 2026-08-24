package com.fintech.accountcontract.contract;

import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import java.util.UUID;

public interface AccountQueryContract {
    CustomerAccountsResponse fetchCustomerAccountDetails(UUID customerId);
}
