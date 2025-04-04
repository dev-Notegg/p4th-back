package com.p4th.backend.common.exception;

import com.p4th.backend.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, Locale locale) {
        ErrorCode code = ex.getErrorCode();
        String message = messageSource.getMessage(code.getMessage(), null, locale);
        // CustomException의 경우, 간단한 메시지만 로그에 남김.
        logger.error("CustomException : " + message, ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(code.getCode())
                .errorMessage(message)
                .status(code.getHttpStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, code.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> errors = bindingResult.getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (FieldError fieldError : errors) {
            errorMessage.append(fieldError.getField())
                    .append(" ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(400)
                .errorMessage(errorMessage.toString())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        logger.error("Unreadable HTTP message: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(400)
                .errorMessage("Malformed JSON request: " + ex.getMostSpecificCause().getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        logger.error("Method not supported: {}", ex.getMessage());
        String message = "HTTP method " + ex.getMethod() + " is not supported. Supported methods: " + ex.getSupportedHttpMethods();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(405)
                .errorMessage(message)
                .status(HttpStatus.METHOD_NOT_ALLOWED.name())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        // 그 외의 예외는 전체 스택 트레이스를 로그에 남김.
        logger.error("Unhandled exception: ", ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(500)
                .errorMessage("Internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
