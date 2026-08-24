package com.fintech.customer.domain;

public enum CustomerStatus {
    INITIATED("INITIATED","Customer account creation initiated."),
    ACCOUNT_CREATED("ACCOUNT_CREATED","Customer wallet creation initiated."),
    WALLET_CREATED("WALLET_CREATED","Customer ledger account creation initiated."),
    LEDGER_ACCOUNT_CREATED("LEDGER_ACCOUNT_CREATED","Customer account activation initiated."),
    ACCOUNT_ACTIVATED("ACCOUNT_ACTIVATED","Customer account activated successfully."),
    ACCOUNT_SUSPENDED("ACCOUNT_SUSPENDED","Customer account suspended"),
    ACCOUNT_CLOSED("ACCOUNT_CLOSED","Customer account closed."),
    CUSTOMER_ONBOARDING_FAILED("CUSTOMER_ONBOARDING_FAILED","Customer onboarding failed"),;

    private final String status;
    private final String statusDetail;

    CustomerStatus(String status, String statusDetail) {
        this.status = status;
        this.statusDetail = statusDetail;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public static String fetchDetailFromStatus(String status){
        return CustomerStatus.valueOf(status).getStatusDetail();
    }
}
