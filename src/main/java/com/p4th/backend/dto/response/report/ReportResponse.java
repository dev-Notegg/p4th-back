package com.p4th.backend.dto.response.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "신고 응답 DTO")
public class ReportResponse {
    @Schema(description = "신고 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String reportId;
}
