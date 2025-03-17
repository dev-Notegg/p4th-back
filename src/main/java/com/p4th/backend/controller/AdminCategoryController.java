package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.dto.request.*;
import com.p4th.backend.dto.response.admin.BoardListResponse;
import com.p4th.backend.dto.response.admin.CategoryCreationResponse;
import com.p4th.backend.dto.response.admin.CategoryResponse;
import com.p4th.backend.security.Authorization;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카테고리 관리 API", description = "카테고리 관리 관련 API")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;
    private final Authorization authorization;
    private final JwtProvider jwtProvider;

    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록을 조회하며, 카테고리 ID 또는 카테고리명으로 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getCategories(
            @Parameter(name = "categoryId", description = "검색할 카테고리 ID (옵션)")
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @Parameter(name = "categoryName", description = "검색할 카테고리명 (옵션)")
            @RequestParam(value = "categoryName", required = false) String categoryName,
            HttpServletRequest request,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        authorization.checkAdmin(request);
        Page<CategoryResponse> response = adminCategoryService.getCategories(categoryId, categoryName, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메인 노출 설정 변경", description = "특정 카테고리의 메인 노출 설정을 변경한다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메인 노출 설정 변경 성공"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{categoryId}/main-exposure")
    public ResponseEntity<?> updateMainExposure(
            @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
            @PathVariable("categoryId") String categoryId,
            @RequestBody MainExposureUpdateRequest requestDto,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        String userId = jwtProvider.resolveUserId(request);
        adminCategoryService.updateMainExposure(userId, categoryId, requestDto.getMainExposure());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 추가", description = "새 카테고리를 추가한다. sortOrder는 현재 최대값+1, mainExposure는 0으로 설정된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 추가 성공",
                    content = @Content(schema = @Schema(implementation = CategoryCreationResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "카테고리명 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryCreationResponse> createCategory(
            @RequestBody CategoryCreationRequest requestDto,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        String userId = jwtProvider.resolveUserId(request);
        String categoryId = adminCategoryService.createCategory(userId, requestDto.getCategoryName());
        return ResponseEntity.ok(new CategoryCreationResponse(categoryId));
    }

    @Operation(summary = "카테고리 순서 변경", description = "전체 카테고리의 노출 순서를 변경한다. 모든 카테고리의 ID를 순서대로 전송해야 한다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "카테고리 순서 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "입력값 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/order")
    public ResponseEntity<?> updateCategoryOrder(
            @RequestBody CategoryOrderUpdateRequest requestDto,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        String userId = jwtProvider.resolveUserId(request);
        adminCategoryService.updateCategoryOrder(userId, requestDto.getOrder());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 내 게시판 목록 조회", description = "특정 카테고리에 속한 게시판 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardListResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{categoryId}/boards")
    public ResponseEntity<BoardListResponse> getBoardsByCategory(
            @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
            @PathVariable("categoryId") String categoryId,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        BoardListResponse response = adminCategoryService.getBoardsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시판 노출 순서 변경", description = "전체 게시판의 노출 순서를 변경한다.\n" +
            "정렬 타입이 'postCount'인 경우 게시글 수 기준으로 자동 정렬된다. 기본 수동 정렬은 'normal'로 요청한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 순서 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{categoryId}/boards/order")
    public ResponseEntity<?> updateBoardOrder(
            @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
            @PathVariable("categoryId") String categoryId,
            @RequestBody BoardOrderUpdateRequest requestDto,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        String userId = jwtProvider.resolveUserId(request);
        adminCategoryService.updateBoardOrder(userId, categoryId, requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리명 수정", description = "특정 카테고리의 이름을 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리명 수정 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "카테고리명 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{categoryId}/category-name")
    public ResponseEntity<?> updateCategoryName(
            @Parameter(name = "categoryId", description = "수정할 카테고리 ID", required = true)
            @PathVariable("categoryId") String categoryId,
            @RequestBody CategoryNameUpdateRequest requestDto,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        String userId = jwtProvider.resolveUserId(request);
        adminCategoryService.updateCategoryName(userId, categoryId, requestDto.getCategoryName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(name = "categoryId", description = "삭제할 카테고리 ID", required = true)
            @PathVariable("categoryId") String categoryId,
            HttpServletRequest request) {
        authorization.checkAdmin(request);
        adminCategoryService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }
}
