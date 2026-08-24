package com.fintech.transaction.exception;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.ErrorCode;
import com.fintech.common.exception.ErrorResponse;
import com.fintech.common.exception.ValidationErrorResponseMapper;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransactionExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {

        ErrorCode code = ex.getErrorCode();
        HttpStatus status = mapToHttpStatus(code);

        if(ex.getFieldErrorDetailList() != null){
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(ex, ex.getFieldErrorDetailList(),
                            MDC.get("traceId")));
        }else {
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(ex, MDC.get("traceId")));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        ErrorResponse response = ValidationErrorResponseMapper
                .fromBindingResult(ex.getBindingResult());
        return ResponseEntity.badRequest().body(response);
    }

    private HttpStatus mapToHttpStatus(ErrorCode code) {
        return switch (code.getErrorType()) {
            case BUSINESS -> HttpStatus.BAD_REQUEST;
            case TRANSIENT -> HttpStatus.SERVICE_UNAVAILABLE;
            case TECHNICAL, FATAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
