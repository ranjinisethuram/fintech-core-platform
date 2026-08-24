package com.fintech.customer.dto;

public class CustomerStatusResponse {

    private String customerId;
    private String status;
    private String statusDetails;

    public CustomerStatusResponse(String customerId, String status, String statusDetails){
        this.customerId = customerId;
        this.status = status;
        this.statusDetails = statusDetails;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    public void setStatusDetails(String statusDetails) {
        this.statusDetails = statusDetails;
    }
}
