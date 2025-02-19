package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "스크랩 폴더 생성 요청 DTO")
public class ScrapFolderCreateRequest {
    @Schema(description = "폴더명", example = "새 스크랩 폴더")
    private String folderName;
}
