package com.p4th.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 예시) 회원가입/로그인 관련
    PHONE_DUPLICATED(HttpStatus.CONFLICT, 10201, "PHONE_DUPLICATED"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, 10202, "USER_NOT_FOUND"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 10203, "INVALID_PASSWORD"),

    // JWT, 인증 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 10100, "AUTHENTICATION_FAILED"),
    ;

    private final HttpStatus httpStatus;
    private final int code;          // 내부 비즈니스 에러코드
    private final String message;    // 메시지

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
