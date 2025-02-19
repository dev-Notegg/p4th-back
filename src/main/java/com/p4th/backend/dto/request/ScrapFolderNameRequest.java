package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "스크랩 폴더명 변경/생성 요청 DTO")
public class ScrapFolderNameRequest {
    @Schema(description = "폴더명", example = "폴더명")
    private String folderName;
}
