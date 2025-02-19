package com.p4th.backend.dto.response.scrap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "게시글 스크랩 생성 응답 DTO")
public class ScrapCreateResponse {
    @Schema(description = "스크랩 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapId;
}
