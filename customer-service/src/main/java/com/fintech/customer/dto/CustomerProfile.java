package com.fintech.customer.dto;

import com.fintech.accountcontract.dto.AccountSummary;

import java.util.List;

public class CustomerProfile {

    private String customerId;
    private String customerName;
    private String defaultAccountId;
    private List<AccountSummary> accounts;
    private List<BeneficiaryResponse> beneficiaries;


    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDefaultAccountId() {
        return defaultAccountId;
    }

    public void setDefaultAccountId(String defaultAccountId) {
        this.defaultAccountId = defaultAccountId;
    }

    public List<AccountSummary> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountSummary> accounts) {
        this.accounts = accounts;
    }

    public java.util.List<BeneficiaryResponse> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiaryResponse> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }
}
