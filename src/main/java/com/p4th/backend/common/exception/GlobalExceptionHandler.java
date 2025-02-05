package com.p4th.backend.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .errorCode(code.getCode())
                .errorMessage(code.getMessage())
                .status(code.getHttpStatus().name())
                .build();

        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }
}
