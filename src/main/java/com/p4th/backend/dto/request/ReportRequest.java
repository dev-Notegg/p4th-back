package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "신고 요청 DTO")
public class ReportRequest {
    @Schema(description = "신고 대상 타입 (POST 또는 COMMENT)", example = "POST")
    private String targetType;

    @Schema(description = "신고 대상 회원ID", example = "yr0408")
    private String targetUserId;

    @Schema(description = "신고 대상 ID (게시글 또는 댓글 ID)", example = "01JKWBMY3A47MEPETRT8MJN0MZ")
    private String targetId;

    @Schema(description = "신고 사유", example = "부적절한 내용")
    private String reason;
}