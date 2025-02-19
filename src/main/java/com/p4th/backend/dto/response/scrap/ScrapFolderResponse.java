package com.p4th.backend.dto.response.scrap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "스크랩 폴더 응답 DTO")
public class ScrapFolderResponse {
    @Schema(description = "스크랩 폴더 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapFolderId;
    
    @Schema(description = "폴더명", example = "사주")
    private String folderName;
    
    @Schema(description = "정렬 순서", example = "0")
    private int sortOrder;
    
    @Schema(description = "생성일시", example = "2025-02-04 09:00:00")
    private LocalDateTime createdAt;
}
