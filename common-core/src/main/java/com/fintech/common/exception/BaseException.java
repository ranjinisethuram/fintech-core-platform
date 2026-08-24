package com.fintech.common.exception;

import java.util.List;

public class BaseException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String message;
    private List<FieldErrorDetail> fieldErrorDetailList;


    public BaseException(ErrorCode errorCode){
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getErrorMessage();
    }

    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.message = customMessage;
    }

    public BaseException(ErrorCode errorCode, List<FieldErrorDetail> fieldErrorDetailList){
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getErrorMessage();
        this.fieldErrorDetailList = fieldErrorDetailList;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<FieldErrorDetail> getFieldErrorDetailList() {
        return fieldErrorDetailList;
    }
}
