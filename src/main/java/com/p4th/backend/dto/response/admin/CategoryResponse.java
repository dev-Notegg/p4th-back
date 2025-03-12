package com.p4th.backend.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.p4th.backend.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "카테고리 응답 DTO")
public class CategoryResponse {

    @Schema(description = "카테고리 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String categoryId;

    @Schema(description = "카테고리명", example = "일반")
    private String categoryName;

    @Schema(description = "정렬 순서", example = "1")
    private int sortOrder;

    @Schema(description = "메인 노출 여부", example = "1")
    private int mainExposure;

    @Schema(description = "최근 수정/생성일", example = "2025-01-01")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String recentlyModified;

    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setSortOrder(category.getSortOrder());
        response.setMainExposure(category.getMainExposure());

        // 최근 수정일이 있으면 수정일, 없으면 생성일을 'yyyy-MM-dd' 형식으로 설정
        LocalDateTime dateToShow = category.getUpdatedAt() != null ? category.getUpdatedAt() : category.getCreatedAt();
        if(dateToShow != null) {
            response.setRecentlyModified(dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return response;
    }
}