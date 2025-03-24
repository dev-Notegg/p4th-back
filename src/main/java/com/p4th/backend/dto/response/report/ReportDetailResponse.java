package com.p4th.backend.dto.response.report;

import com.p4th.backend.domain.Report;
import com.p4th.backend.domain.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "신고 상세 응답 DTO")
public class ReportDetailResponse {

    @Schema(description = "신고 ID")
    private String reportId;

    @Schema(description = "신고자 ID")
    private String reporterId;

    @Schema(description = "신고 대상 사용자 ID")
    private String targetUserId;

    @Schema(description = "신고 대상 게시글 ID", nullable = true)
    private String targetPostId;

    @Schema(description = "신고 대상 댓글 ID", nullable = true)
    private String targetCommentId;

    @Schema(description = "신고 대상 타입 (POST/COMMENT)")
    private ReportType type;

    @Schema(description = "읽음 여부(0: 미확인, 1: 확인)")
    private int readYn;

    @Schema(description = "읽음 일시 (yyyy-MM-dd HH:mm:ss)", nullable = true)
    private String readAt;

    @Schema(description = "신고 사유(전체)")
    private String reason;

    @Schema(description = "신고 생성일 (yyyy-MM-dd HH:mm:ss)")
    private String createdAt;

    public static ReportDetailResponse from(Report report) {
        ReportDetailResponse dto = new ReportDetailResponse();
        dto.setReportId(report.getReportId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetUserId(report.getTargetUserId());
        dto.setTargetPostId(report.getTargetPostId());
        dto.setTargetCommentId(report.getTargetCommentId());
        dto.setType(report.getType());
        dto.setReadYn(report.getReadYn());
        if (report.getReadAt() != null) {
            dto.setReadAt(report.getReadAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setReason(report.getReason());
        if (report.getCreatedAt() != null) {
            dto.setCreatedAt(report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }
}
