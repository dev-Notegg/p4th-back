package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "카테고리 순서 변경 요청 DTO")
public class CategoryOrderUpdateRequest {
    @Schema(description = "변경된 카테고리 순서 (카테고리 ID 리스트)", example = "[\"cat-uuid-001\", \"cat-uuid-002\", \"cat-uuid-003\"]")
    private List<String> order;
}
