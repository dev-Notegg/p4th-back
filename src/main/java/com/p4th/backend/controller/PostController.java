package com.p4th.backend.controller;

import com.p4th.backend.domain.Post;
import com.p4th.backend.dto.PageResponse;
import com.p4th.backend.dto.PopularPostResponse;
import com.p4th.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "게시글 API", description = "게시글 관련 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final com.p4th.backend.security.JwtProvider jwtProvider;

    @Operation(summary = "게시글 목록 조회", description = "board_id, page, size를 사용하여 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<Post>> getPostsByBoard(
            @RequestParam("board_id") String boardId,
            @RequestParam int page,
            @RequestParam int size) {
        PageResponse<Post> result = postService.getPostsByBoard(boardId, page, size);
        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "게시글 상세 조회", description = "postId를 입력받아 게시글 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostDetail(@PathVariable("postId") String postId) {
        Post post = postService.getPostDetail(postId);
        return ResponseEntity.ok().body(post);
    }

    @Operation(summary = "게시글 등록(첨부파일 포함)", description = "게시글 작성 및 첨부파일 업로드를 한 번에 처리합니다. 토큰에서 회원ID를 추출하여 사용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<CreatePostResponse> registerPost(
            @RequestParam String boardId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        String postId = postService.registerPostWithAttachments(boardId, userId, title, content, files);
        CreatePostResponse response = new CreatePostResponse(postId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 수정(첨부파일 포함)", description = "게시글 수정 API. 토큰의 회원ID와 게시글 작성자가 일치해야 수정 가능합니다. 기존 첨부파일은 모두 삭제되고, 신규 첨부파일로 교체됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @PutMapping(value = "/with-attachments/{postId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<UpdatePostResponse> updatePostWithAttachments(
            @PathVariable("postId") String postId,
            @RequestParam String boardId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        postService.updatePostWithAttachments(postId, new UpdatePostRequest(boardId, userId, title, content), newFiles);
        UpdatePostResponse response = new UpdatePostResponse(postId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 API. 토큰의 회원ID와 게시글 작성자가 일치해야 삭제 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(@PathVariable("postId") String postId,
                                                         HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        postService.deletePost(postId, userId);
        DeletePostResponse response = new DeletePostResponse(true);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "인기 게시글 목록 조회", description = "스케줄러에서 계산한 post_history_log를 조회하여, 인기 게시글 목록(최대 20개)을 반환합니다. period 파라미터(DAILY, WEEKLY, MONTHLY)를 통해 조회 기간을 지정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopularPostResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<PopularPostResponse>> getPopularPosts(@RequestParam(value = "period", defaultValue = "DAILY") String period) {
        List<PopularPostResponse> popularPosts = postService.getPopularPosts(period);
        return ResponseEntity.ok().body(popularPosts);
    }

    // 내부 DTO 클래스
    @Data
    public static class CreatePostResponse {
        private String postId;
        public CreatePostResponse(String postId) {
            this.postId = postId;
        }
    }

    @Data
    public static class UpdatePostRequest {
        private String boardId;
        private String userId;
        private String title;
        private String content;

        public UpdatePostRequest(String boardId, String userId, String title, String content) {
            this.boardId = boardId;
            this.userId = userId;
            this.title = title;
            this.content = content;
        }
    }

    @Data
    public static class UpdatePostResponse {
        private String postId;
        public UpdatePostResponse(String postId) {
            this.postId = postId;
        }
    }

    @Data
    public static class DeletePostResponse {
        private boolean deleted;
        public DeletePostResponse(boolean deleted) {
            this.deleted = deleted;
        }
    }
}
