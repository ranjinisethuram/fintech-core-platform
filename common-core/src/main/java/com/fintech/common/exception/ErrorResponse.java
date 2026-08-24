package com.fintech.common.exception;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    private String errorCode;
    private String message;
    private List<FieldErrorDetail> errorDetails;
    private String errorType;
    private boolean retryable;
    private String path;
    private String traceId;
    private Instant timestamp;

    public ErrorResponse(BaseException ex, List<FieldErrorDetail> errorDetails, String traceId){
        this.errorCode = ex.getErrorCode().getErrorCode();
        this.message = ex.getMessage();
        this.errorDetails = errorDetails;
        this.errorType = ex.getErrorCode().getErrorType().name();
        this.retryable = ex.getErrorCode().isRetryable();
        this.traceId = traceId;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(BaseException ex, String traceId){
        this.errorCode = ex.getErrorCode().getErrorCode();
        this.message = ex.getMessage();
        this.errorType = ex.getErrorCode().getErrorType().name();
        this.retryable = ex.getErrorCode().isRetryable();
        this.traceId = traceId;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(ErrorCode errorCode, List<FieldErrorDetail> errorDetails, String traceId){
        this.errorCode = errorCode.getErrorCode();
        this.message = errorCode.getErrorMessage();
        this.errorDetails = errorDetails;
        this.errorType = errorCode.getErrorType().name();
        this.retryable = errorCode.isRetryable();
        this.traceId = traceId;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(ErrorCode errorCode, String requestPath, String traceId){
        this.errorCode = errorCode.getErrorCode();
        this.message = errorCode.getErrorMessage();
        this.errorType = errorCode.getErrorType().name();
        this.retryable = errorCode.isRetryable();
        this.path = requestPath;
        this.traceId = traceId;
        this.timestamp = Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<FieldErrorDetail> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
