package com.p4th.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 회원가입/로그인 관련
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, 10202, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 10203, "비밀번호가 올바르지 않습니다."),
    INVALID_PASSCODE(HttpStatus.BAD_REQUEST, 10204, "패쓰코드가 올바르지 않습니다."),

    // JWT, 인증 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 10100, "인증에 실패하였습니다."),

    // S3 업로드 실패
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 50100, "S3 업로드에 실패하였습니다."),

    // 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "내부 서버 오류가 발생하였습니다."),

    // 권한 없음
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, 40300, "권한이 없습니다."),

    //게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "게시글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
