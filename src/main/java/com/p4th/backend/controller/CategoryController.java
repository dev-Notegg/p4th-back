package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.Category;
import com.p4th.backend.common.CommonResponse;
import com.p4th.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리 API", description = "카테고리 관련 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "전체 카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 기타 문제",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public CommonResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return CommonResponse.success(categories);
    }

    @Operation(summary = "특정 카테고리에 속한 게시판 목록 조회", description = "특정 카테고리 ID를 입력받아 해당 카테고리 정보와 게시판 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 내 게시판 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 기타 문제",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{categoryId}/boards")
    public CommonResponse<Category> getBoardsByCategory(@PathVariable String categoryId) {
        Category category = categoryService.getBoardsByCategory(categoryId);
        return CommonResponse.success(category);
    }
}
