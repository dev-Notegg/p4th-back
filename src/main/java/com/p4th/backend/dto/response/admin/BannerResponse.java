package com.p4th.backend.dto.response.admin;

import com.p4th.backend.domain.Banner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "배너 응답 DTO")
public class BannerResponse {
    @Schema(description = "배너 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String bannerId;
    
    @Schema(description = "광고식별명", example = "메인배너")
    private String bannerName;
    
    @Schema(description = "배너 이미지 URL", example = "http://example.com/banner1.jpg")
    private String imageUrl;
    
    @Schema(description = "클릭 시 이동 링크", example = "http://example.com")
    private String linkUrl;
    
    @Schema(description = "배너 노출 순서", example = "1")
    private Integer seq;
    
    @Schema(description = "광고 시작일", example = "2025-02-01 00:00:00")
    private String startDate;
    
    @Schema(description = "광고 종료일", example = "2025-02-28 23:59:59")
    private String endDate;

    public static BannerResponse from(Banner banner) {
        BannerResponse dto = new BannerResponse();
        dto.setBannerId(banner.getBannerId());
        dto.setBannerName(banner.getBannerName());
        dto.setImageUrl(banner.getImageUrl());
        dto.setLinkUrl(banner.getLinkUrl());
        dto.setSeq(banner.getSeq());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setStartDate(banner.getStartDate() != null ? banner.getStartDate().format(dtf) : null);
        dto.setEndDate(banner.getEndDate() != null ? banner.getEndDate().format(dtf) : null);
        return dto;
    }
}
