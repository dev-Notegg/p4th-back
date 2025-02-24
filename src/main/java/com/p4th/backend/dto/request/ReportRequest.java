package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "신고 요청 DTO")
public class ReportRequest {
    @Schema(description = "신고 대상 타입 ('POST' 또는 'COMMENT')", example = "POST")
    private String type;
    
    @Schema(description = "신고 사유", example = "부적절한 내용")
    private String reason;
}
