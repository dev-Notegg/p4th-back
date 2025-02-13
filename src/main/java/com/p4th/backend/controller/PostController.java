package com.p4th.backend.controller;

import com.p4th.backend.domain.Post;
import com.p4th.backend.dto.request.RegisterPostRequest;
import com.p4th.backend.dto.request.UpdatePostRequest;
import com.p4th.backend.dto.response.*;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostListDto>> getPostsByBoard(
            @Parameter(name = "board_id", description = "게시판 ID", required = true)
            @RequestParam("board_id") String boardId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListDto> posts = postService.getPostsByBoard(boardId, pageable);
        return ResponseEntity.ok().body(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "postId를 입력받아 게시글 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostDetail(
            @Parameter(name = "postId", description = "게시글 ID", required = true) @PathVariable("postId") String postId) {
        Post post = postService.getPostDetail(postId);
        PostResponseDto responseDto = PostResponseDto.from(post);
        return ResponseEntity.ok().body(responseDto);
    }

    @Operation(summary = "게시글 등록", description = "게시글 작성을 처리한다. 토큰에서 회원ID를 추출하여 사용하며, 클라이언트는 HTML 콘텐츠(내부 미디어 포함)를 JSON 형식으로 전송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @PutMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatePostResponse> updatePost(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @Parameter(name = "UpdatePostRequest", description = "게시글 수정 요청 DTO", required = true)
            @RequestBody UpdatePostRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        postService.updatePost(postId, request.getBoardId(), userId, request.getTitle(), request.getContent());
        UpdatePostResponse response = new UpdatePostResponse(postId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 API. 토큰의 회원ID와 게시글 작성자가 일치해야 삭제 가능하다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
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

    @Operation(summary = "인기 게시글 목록 조회", description = "인기 게시글 목록(최대 20개)을 반환한다. period 파라미터(DAILY, WEEKLY, MONTHLY)를 통해 조회 기간을 지정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopularPostResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<?>> getPopularPosts(
            @Parameter(name = "period", description = "조회 기간 (DAILY, WEEKLY, MONTHLY)", example = "DAILY")
            @RequestParam(value = "period", defaultValue = "DAILY") String period) {
        List<?> popularPosts = postService.getPopularPosts(period);
        return ResponseEntity.ok().body(popularPosts);
    }
}
