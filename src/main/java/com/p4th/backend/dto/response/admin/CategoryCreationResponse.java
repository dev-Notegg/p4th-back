package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "카테고리 추가 응답 DTO")
public class CategoryCreationResponse {
    @Schema(description = "생성된 카테고리 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String categoryId;

    public CategoryCreationResponse(String categoryId) {
        this.categoryId = categoryId;
    }
}
