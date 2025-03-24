package com.p4th.backend.controller;

import com.p4th.backend.annotation.RequireLogin;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.Category;
import com.p4th.backend.dto.response.board.BoardResponse;
import com.p4th.backend.dto.response.user.UserCommentPostResponse;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.MenuService;
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

import java.util.List;

@Tag(name = "메뉴 API", description = "햄버거 메뉴 관련 API (내 계정 조회 제외)")
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "최근 본 게시물 목록 조회", description = "최근에 본 게시글(최대 15개)을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 본 게시물 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @GetMapping(value = "/recent-posts")
    public ResponseEntity<List<PostListResponse>> getRecentPosts(
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        List<PostListResponse> posts = menuService.getRecentPosts(userId);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "작성한 글 목록 조회", description = "내가 작성한 게시글을 최신순으로 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성한 글 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @GetMapping(value = "/{userId}/posts")
    public ResponseEntity<Page<PostListResponse>> getUserPosts(
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        Page<PostListResponse> posts = menuService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "내가 쓴 댓글 목록 조회", description = "내가 작성한 댓글이 포함된 게시글 목록을 조회한다. 각 게시글에는 내가 쓴 댓글 정보가 포함된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 쓴 댓글 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @GetMapping(value = "/{userId}/comments")
    public ResponseEntity<Page<UserCommentPostResponse>> getUserComments(
            @Parameter(hidden = true) Pageable pageable,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        Page<UserCommentPostResponse> responses = menuService.getUserComments(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "전체 카테고리 목록 조회", description = "전체 카테고리 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = menuService.getAllCategories();
        return ResponseEntity.ok().body(categories);
    }

    @Operation(summary = "특정 카테고리에 속한 게시판 목록 조회", description = "특정 카테고리 ID를 입력받아 해당 카테고리 정보와 게시판 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 내 게시판 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{categoryId}/boards")
    public ResponseEntity<List<BoardResponse>> getBoardsByCategory(
            @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
            @PathVariable String categoryId) {
        List<BoardResponse> boards = menuService.getBoardsByCategory(categoryId);
        return ResponseEntity.ok().body(boards);
    }
}
