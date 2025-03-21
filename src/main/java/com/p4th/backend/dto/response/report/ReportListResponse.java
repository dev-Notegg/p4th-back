package com.p4th.backend.dto.response.report;

import com.p4th.backend.domain.Report;
import com.p4th.backend.domain.ReportType;
import com.p4th.backend.util.HtmlContentUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "신고 목록 응답 DTO")
public class ReportListResponse {

    @Schema(description = "신고 ID")
    private String reportId;

    @Schema(description = "신고자 ID")
    private String reporterId;

    @Schema(description = "신고 대상 사용자 ID")
    private String targetUserId;

    @Schema(description = "신고 대상 타입 (POST/COMMENT)")
    private ReportType type;

    @Schema(description = "신고 사유")
    private String reason;

    @Schema(description = "읽음 여부(0: 미확인, 1: 확인)")
    private int readYn;

    @Schema(description = "신고 생성일 (yyyy-MM-dd)")
    private String createdAt;

    public static ReportListResponse from(Report report) {
        ReportListResponse dto = new ReportListResponse();
        dto.setReportId(report.getReportId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetUserId(report.getTargetUserId());
        dto.setType(report.getType());
        dto.setReason(HtmlContentUtils.extractPlainText(report.getReason(),30));
        dto.setReadYn(report.getReadYn());
        if (report.getCreatedAt() != null) {
            dto.setCreatedAt(report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return dto;
    }
}
