package com.p4th.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 통합 에러 응답 DTO
 * API 응답용 에러 정보를 담으며, 모든 에러 응답에 일관되게 사용됩니다.
 */
@Data
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