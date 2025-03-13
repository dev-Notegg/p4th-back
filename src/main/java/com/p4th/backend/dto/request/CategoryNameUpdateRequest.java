package com.p4th.backend.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CategoryNameUpdateRequest {
    @Schema(description = "카테고리명", example = "사주")
    private String categoryName;
}
