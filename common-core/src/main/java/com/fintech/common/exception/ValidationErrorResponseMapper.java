package com.fintech.common.exception;

import org.slf4j.MDC;
import org.springframework.validation.BindingResult;

import java.util.List;

public class ValidationErrorResponseMapper {

    public static ErrorResponse fromBindingResult(BindingResult result) {
        List<FieldErrorDetail> errors = result.getFieldErrors()
                .stream()
                .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return new ErrorResponse(CommonErrorCode.VALIDATION_FAILED, errors
                , MDC.get("traceId"));
    }
}
