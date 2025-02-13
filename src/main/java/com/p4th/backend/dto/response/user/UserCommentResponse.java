package com.p4th.backend.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.p4th.backend.util.RelativeTimeFormatter;

@Data
@Schema(description = "내가 쓴 댓글 응답 DTO")
public class UserCommentResponse {
    @Schema(description = "댓글 ID")
    private String commentId;

    @Schema(description = "댓글 작성자 ID")
    private String userId;

    @Schema(description = "댓글 작성자 닉네임")
    private String nickname;

    @Schema(description = "게시글 작성자 ID")
    private String writerLoginId;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "댓글 작성일 (상대 시간 형식)")
    private String createdAt;

    public static UserCommentResponse from(com.p4th.backend.domain.Comment comment) {
        UserCommentResponse dto = new UserCommentResponse();
        dto.setCommentId(comment.getCommentId());
        dto.setUserId(comment.getUserId());
        dto.setNickname(comment.getNickname() != null ? comment.getNickname() : "");
        // writerLoginId는 서비스 로직에서 게시글 조회 시 별도로 설정 (예: Post.userId)
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt() != null ?
                RelativeTimeFormatter.formatRelativeTime(comment.getCreatedAt()) : null);
        return dto;
    }
}
