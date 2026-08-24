package com.fintech.accountcontract.dto;

import java.util.List;

public class CustomerAccountsResponse {
    String customerId;
    List<AccountSummary> customerAccounts;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<AccountSummary> getCustomerAccounts() {
        return customerAccounts;
    }

    public void setCustomerAccounts(List<AccountSummary> customerAccounts) {
        this.customerAccounts = customerAccounts;
    }
}
