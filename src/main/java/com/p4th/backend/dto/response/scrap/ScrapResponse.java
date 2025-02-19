package com.p4th.backend.dto.response.scrap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "게시글 스크랩 응답 DTO")
public class ScrapResponse {
    @Schema(description = "스크랩 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapId;
    
    @Schema(description = "게시글 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String postId;
    
    @Schema(description = "스크랩 일시", example = "2025-02-04 10:10:00")
    private LocalDateTime scrappedAt;
}
