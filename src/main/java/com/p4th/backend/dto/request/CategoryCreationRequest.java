package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "카테고리 추가 요청 DTO")
public class CategoryCreationRequest {
    @Schema(description = "카테고리명", example = "새 카테고리")
    private String categoryName;
}
