package com.fintech.transaction.dto;

import java.math.BigDecimal;

public class TransactionStatusResponse {
    private String transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;

    public TransactionStatusResponse() {}

    public TransactionStatusResponse(String transactionId, String status, BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
