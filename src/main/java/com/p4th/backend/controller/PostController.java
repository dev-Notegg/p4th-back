package com.p4th.backend.controller;

import com.p4th.backend.domain.Post;
import com.p4th.backend.common.CommonResponse;
import com.p4th.backend.dto.PageResponse;
import com.p4th.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Tag(name = "게시글 API", description = "게시글 관련 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "board_id, page, size를 사용하여 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping
    public CommonResponse<PageResponse<Post>> getPostsByBoard(
            @RequestParam("board_id") String boardId,
            @RequestParam int page,
            @RequestParam int size) {
        PageResponse<Post> result = postService.getPostsByBoard(boardId, page, size);
        return CommonResponse.success(result);
    }

    @Operation(summary = "게시글 상세 조회", description = "postId를 입력받아 게시글 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping("/{postId}")
    public CommonResponse<Post> getPostDetail(@PathVariable("postId") String postId) {
        Post post = postService.getPostDetail(postId);
        return CommonResponse.success(post);
    }

    @Operation(summary = "게시글 등록(첨부파일 포함)", description = "게시글 작성 및 첨부파일 업로드를 한 번에 처리합니다. 게시글 생성 후 생성된 게시글 ID를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponse<CreatePostResponse> registerPost(
            @RequestParam String boardId,
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String postId = postService.registerPostWithAttachments(boardId, userId, title, content, files);
        CreatePostResponse response = new CreatePostResponse(postId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "게시글 수정(첨부파일 포함)", description = "게시글 수정 API. 본문 수정과 함께 신규 첨부파일 추가 및 삭제할 첨부파일 ID 목록을 전달하여 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @PutMapping(value = "/with-attachments/{postId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponse<UpdatePostResponse> updatePostWithAttachments(
            @PathVariable("postId") String postId,
            @RequestParam String boardId,
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            @RequestParam(value = "removeAttachmentIds", required = false) List<String> removeAttachmentIds) {
        PostController.UpdatePostRequest updateRequest = new PostController.UpdatePostRequest();
        updateRequest.setBoardId(boardId);
        updateRequest.setUserId(userId);
        updateRequest.setTitle(title);
        updateRequest.setContent(content);
        postService.updatePostWithAttachments(postId, updateRequest, newFiles, removeAttachmentIds);
        UpdatePostResponse response = new UpdatePostResponse(postId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @DeleteMapping("/{postId}")
    public CommonResponse<DeletePostResponse> deletePost(@PathVariable("postId") String postId) {
        postService.deletePost(postId);
        DeletePostResponse response = new DeletePostResponse(true);
        return CommonResponse.success(response);
    }

    // 내부 DTO 클래스
    @lombok.Data
    public static class CreatePostResponse {
        private String postId;
        public CreatePostResponse(String postId) {
            this.postId = postId;
        }
    }

    @lombok.Data
    public static class UpdatePostRequest {
        private String boardId;
        private String userId;
        private String title;
        private String content;
    }

    @lombok.Data
    public static class UpdatePostResponse {
        private String postId;
        public UpdatePostResponse(String postId) {
            this.postId = postId;
        }
    }

    @lombok.Data
    public static class DeletePostResponse {
        private boolean deleted;
        public DeletePostResponse(boolean deleted) {
            this.deleted = deleted;
        }
    }
}
