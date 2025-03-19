package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "배너 등록 요청 DTO")
public class BannerCreationRequest {
    @Schema(description = "광고식별명", example = "메인배너")
    private String bannerName;
    
    @Schema(description = "클릭 시 이동 링크", example = "https://example.com/landing")
    private String linkUrl;
    
    @Schema(description = "광고 시작일 (yyyy-MM-dd 형식)", example = "2025-03-01")
    private String startDate;
    
    @Schema(description = "광고 종료일 (yyyy-MM-dd 형식)", example = "2025-03-31")
    private String endDate;
}
