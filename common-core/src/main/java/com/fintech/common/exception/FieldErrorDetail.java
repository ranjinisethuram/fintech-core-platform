package com.fintech.common.exception;

public class FieldErrorDetail {

    private String field;
    private String reason;

    public FieldErrorDetail(String field, String reason){
        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
