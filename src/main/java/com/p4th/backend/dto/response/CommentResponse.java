package com.p4th.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.p4th.backend.domain.Comment;
import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "댓글 조회 응답 DTO")
public class CommentResponse {
    
    @Schema(description = "댓글 ID", example = "01HXYAAX6B1N8QZQF4RZBHY1XV")
    private String commentId;
    
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
    private String parentCommentId;
    
    @Schema(description = "작성자 ID", example = "user_002")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "닉네임_001")
    private String nickname;
    
    @Schema(description = "댓글 내용", example = "첫 번째 댓글입니다.")
    private String content;

    @Schema(description = "작성일자", example = "2025-02-12 15:00:01")
    private String createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setParentCommentId(comment.getParentCommentId());
        response.setUserId(comment.getUserId());
        response.setNickname(comment.getNickname() != null ? comment.getNickname() : "");
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : null);
        return response;
    }
}
