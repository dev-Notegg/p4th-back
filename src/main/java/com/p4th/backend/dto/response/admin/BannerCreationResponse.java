package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "배너 등록 응답 DTO")
public class BannerCreationResponse {
    @Schema(description = "생성된 배너 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String bannerId;
}
