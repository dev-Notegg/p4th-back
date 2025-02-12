package com.p4th.backend.dto.response;

import com.p4th.backend.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "게시글 상세 조회 응답 DTO")
public class PostResponseDto {

    @Schema(description = "게시글 ID", example = "01HXYA5X2V6N3QZQF9LZBHY7TW")
    private String postId;

    @Schema(description = "게시판 ID", example = "01HXYA4V8D5N1PZQF8KZBHY6XT")
    private String boardId;

    @Schema(description = "작성자 ID", example = "user_001")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "닉네임")
    private String nickname;

    @Schema(description = "게시글 제목", example = "첫 번째 게시글")
    private String title;

    @Schema(description = "게시글 내용", example = "이것은 첫 번째 게시글 내용입니다.22")
    private String content;

    @Schema(description = "조회수", example = "18")
    private int viewCount;

    @Schema(description = "댓글 수", example = "2")
    private int commentCount;

    @Schema(description = "게시글 생성일시", example = "2025-02-10 16:11:22")
    private String createdAt;

    @Schema(description = "게시글 생성자", example = "user_001")
    private String createdBy;

    @Schema(description = "댓글 목록")
    private List<CommentResponse> comments;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostResponseDto from(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(post.getPostId());
        dto.setBoardId(post.getBoardId());
        dto.setUserId(post.getUserId());
        dto.setNickname(post.getNickname());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setViewCount(post.getViewCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : null);
        dto.setCreatedBy(post.getCreatedBy());
        if (post.getComments() != null) {
            dto.setComments(post.getComments().stream()
                    .map(CommentResponse::from)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
