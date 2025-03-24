package com.p4th.backend.dto.request;

import com.p4th.backend.domain.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "신고 요청 DTO")
public class ReportRequest {
    @Schema(description = "신고 대상 타입 (POST 또는 COMMENT)", example = "POST")
    private ReportType targetType;

    @Schema(description = "신고 대상 회원ID", example = "yr0408")
    private String targetUserId;

    @Schema(description = "신고 대상 게시글 ID", example = "01JKWBMY3A47MEPETRT8MJN0MZ")
    private String targetPostId;

    @Schema(description = "신고 대상 댓글 ID", example = "01JKWBMY3A47MEPETRT8MJN0MZ")
    private String targetCommentId;

    @Schema(description = "신고 사유", example = "부적절한 내용")
    private String reason;
}