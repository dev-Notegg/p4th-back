package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "스크랩 폴더 순서 변경 요청 DTO")
public class ScrapFolderOrderUpdateRequest {
    @Schema(description = "변경된 폴더 순서 (스크랩 폴더 ID 리스트)", example = "[\"scrapfolder-uuid-002\", \"scrapfolder-uuid-001\", \"scrapfolder-uuid-003\"]")
    private List<String> order;
}
