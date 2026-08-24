package com.fintech.transaction.domain;

public enum TransactionStatus {
    INITIATED("INITIATED", "Transaction has been initiated."),
    SUCCESS("SUCCESS","Transaction is success."),
    FAILED("FAILED","Transaction is failed."),
    COMPENSATED("COMPENSATED","Transaction encountered an error. It is being compensated.");

    private final String status;
    private final String statusDetail;

    TransactionStatus(String status, String statusDetail) {
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
        return TransactionStatus.valueOf(status).getStatusDetail();
    }
}
