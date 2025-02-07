package com.p4th.backend.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    /**
     * 에러 코드 (예: 10201, 10100 등)
     */
    private int errorCode;

    /**
     * 에러 메시지
     */
    private String errorMessage;

    /**
     * HTTP 상태 문자열 (예: "BAD_REQUEST", "INTERNAL_SERVER_ERROR")
     */
    private String status;

    /**
     * 에러 발생 시각
     */
    private LocalDateTime timestamp;
}
