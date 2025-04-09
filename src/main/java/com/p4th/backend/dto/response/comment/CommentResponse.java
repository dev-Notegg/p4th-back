package com.p4th.backend.dto.response.comment;

import com.p4th.backend.domain.CommentStatus;
import com.p4th.backend.util.RelativeTimeFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.p4th.backend.domain.Comment;

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

    @Schema(description = "답글 대상자 닉네임 (대대댓글인 경우만)", example = "닉네임_002")
    private String targetNickname;
    
    @Schema(description = "댓글 내용", example = "첫 번째 댓글입니다.")
    private String content;
    
    @Schema(description = "비밀 댓글 여부", example = "true")
    private boolean secretYn;

    @Schema(description = "현재 사용자에게 댓글 내용 공개 여부", example = "true")
    private boolean canViewContent;
    
    @Schema(description = "댓글 작성일 (0분 전, X분 전, X시간 전, 또는 날짜 형식)", example = "0분 전")
    private String createdAt;

    @Schema(description = "댓글 상태", example = "NORMAL")
    private CommentStatus status;

    public static CommentResponse from(Comment comment, String currentUserId, String postAuthorId) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setParentCommentId(comment.getParentCommentId());
        response.setUserId(comment.getUserId());
        response.setNickname(comment.getNickname() != null ? comment.getNickname() : null);
        if (comment.getGrandParentCommentId() != null && comment.getTargetNickname() != null) {
            response.setTargetNickname(comment.getTargetNickname());
        }
        response.setContent(comment.getContent());
        response.setSecretYn(comment.getSecretYn());
        // 만약 댓글이 비밀 댓글이라면, 댓글 작성자 또는 게시글 작성자일 때만 공개, 아니면 false 설정
        if (comment.getSecretYn() != null && comment.getSecretYn()) {
            response.setCanViewContent(comment.getUserId().equals(currentUserId) || postAuthorId.equals(currentUserId));
        } else {
            response.setCanViewContent(true);
        }
        response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(comment.getCreatedAt()));
        response.setStatus(comment.getStatus());
        return response;
    }
}
