package com.p4th.backend.controller;

import com.p4th.backend.dto.request.CommentCreateRequest;
import com.p4th.backend.dto.request.CommentUpdateRequest;
import com.p4th.backend.dto.response.comment.CommentCreateResponse;
import com.p4th.backend.dto.response.comment.CommentResponse;
import com.p4th.backend.dto.response.comment.CommentUpdateResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 API", description = "댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "댓글 목록 조회", description = "게시글의 모든 댓글 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    })
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    })
    @PostMapping(value = "/posts/{postId}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentCreateResponse> createComment(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        String commentId = commentService.createComment(postId, userId, request);
        CommentCreateResponse response = new CommentCreateResponse(commentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 수정", description = "댓글 수정 API. 작성자 본인만 수정할 수 있다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PutMapping(value = "/comments/{commentId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentUpdateResponse> updateComment(
            @Parameter(name = "commentId", description = "댓글 ID", required = true)
            @PathVariable("commentId") String commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        boolean updated = commentService.updateComment(commentId, request.getContent(), userId);
        CommentUpdateResponse response = new CommentUpdateResponse(updated);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 삭제", description = "댓글 삭제 API. 작성자 본인 또는 관리자는 삭제 가능하다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @DeleteMapping(value = "/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteComment(
            @Parameter(name = "commentId", description = "댓글 ID", required = true)
            @PathVariable("commentId") String commentId,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        boolean deleted = commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok().body("{\"deleted\": " + deleted + "}");
    }
}
