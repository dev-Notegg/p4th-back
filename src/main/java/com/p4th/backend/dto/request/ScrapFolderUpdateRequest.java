package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "스크랩 폴더명 변경 요청 DTO")
public class ScrapFolderUpdateRequest {
    @Schema(description = "새로운 폴더명", example = "새로운 폴더명")
    private String folderName;
}
