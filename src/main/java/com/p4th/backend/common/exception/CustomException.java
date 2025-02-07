package com.p4th.backend.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 생성자
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 추가 메시지를 포함하는 생성자
    public CustomException(ErrorCode errorCode, String extraMessage) {
        super(errorCode.getMessage() + " " + extraMessage);
        this.errorCode = errorCode;
    }
}
