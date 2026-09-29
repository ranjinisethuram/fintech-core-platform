package com.fintech.aitool.dto;

import java.util.List;

public class TransactionListResponse {
    private List<TransactionItem> transactions;

    public List<TransactionItem> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionItem> transactions) {
        this.transactions = transactions;
    }

    public static class TransactionItem {
        private String transactionId;
        private String type;
        private java.math.BigDecimal amount;
        private String currency;
        private String status;
        private java.time.Instant timestamp;

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.time.Instant getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
    }
}
