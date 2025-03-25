package com.p4th.backend.controller;

import com.p4th.backend.annotation.RequireLogin;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.dto.request.RegisterPostRequest;
import com.p4th.backend.dto.request.UpdatePostRequest;
import com.p4th.backend.dto.response.post.*;
import com.p4th.backend.service.PostService;
import com.p4th.backend.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "게시글 API", description = "게시글 관련 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "게시글 목록 조회", description = "게시판 ID를 사용하여 게시글 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPostsByBoard(
            @Parameter(name = "board_id", description = "게시판 ID", required = true)
            @RequestParam("board_id") String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        Page<PostListResponse> posts = postService.getPostsByBoard(boardId, userId, pageable);
        return ResponseEntity.ok().body(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "postId를 입력받아 게시글 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @Parameter(name = "postId", description = "게시글 ID", required = true) @PathVariable("postId") String postId,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        Post post = postService.getPostDetail(postId, userId);
        PostDetailResponse responseDto = PostDetailResponse.from(post);
        return ResponseEntity.ok().body(responseDto);
    }

    @Operation(summary = "게시글 등록", description = "게시글 작성을 처리한다. 토큰에서 회원ID를 추출하여 사용하며, 클라이언트는 HTML 콘텐츠(내부 미디어 포함)를 JSON 형식으로 전송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PostMapping
    public ResponseEntity<CreatePostResponse> registerPost(
            @Parameter(name = "RegisterPostRequest", description = "게시글 등록 요청 DTO", required = true)
            @RequestBody RegisterPostRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        String postId = postService.registerPost(request.getBoardId(), userId, request.getTitle(), request.getContent());
        CreatePostResponse response = new CreatePostResponse(postId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 수정", description = "게시글 수정 API. 토큰의 회원ID와 게시글 작성자가 일치해야 수정 가능하다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "삭제된 게시글",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PutMapping(value = "/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @Parameter(name = "UpdatePostRequest", description = "게시글 수정 요청 DTO", required = true)
            @RequestBody UpdatePostRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        postService.updatePost(postId, userId, request.getTitle(), request.getContent());
        UpdatePostResponse response = new UpdatePostResponse(postId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 API. 토큰의 회원ID와 게시글 작성자가 일치해야 삭제 가능하다.")
    @RequireLogin
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "삭제된 게시글",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(
            @Parameter(name = "postId", description = "게시글 ID", required = true) @PathVariable("postId") String postId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        postService.deletePost(postId, userId);
        DeletePostResponse response = new DeletePostResponse(true);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 조회수 증가", description = "게시글의 조회수를 1 증가시킨다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회수 증가 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{postId}/view")
    public ResponseEntity<?> incrementPostView(
            @Parameter(name = "postId", description = "게시글 ID", required = true) @PathVariable("postId") String postId) {
        postService.incrementPostViewCount(postId);
        return ResponseEntity.ok().build();
    }
}
