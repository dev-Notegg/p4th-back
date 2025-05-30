package com.p4th.backend.dto.response.post;

import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.domain.Post;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "게시글 상세 조회 응답 DTO")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "01HXYA5X2V6N3QZQF9LZBHY7TW")
    private String postId;

    @Schema(description = "게시판 ID", example = "01HXYA4V8D5N1PZQF8KZBHY6XT")
    private String boardId;

    @Schema(description = "작성자 ID", example = "user_001")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "닉네임")
    private String nickname;

    private String categoryName;

    private String boardName;

    @Schema(description = "게시글 제목", example = "첫 번째 게시글")
    private String title;

    @Schema(description = "게시글 내용", example = "이것은 첫 번째 게시글 내용입니다.22")
    private String content;

    @Schema(description = "게시글 상태", example = "NORMAL")
    private PostStatus status;

    @Schema(description = "조회수", example = "18")
    private int viewCount;

    @Schema(description = "댓글 수", example = "2")
    private int commentCount;

    //RelativeTimeFormatter를 통해 포맷된 문자열로 반환함
    @Schema(description = "게시글 작성일 (0분 전, X분 전, X시간 전, 또는 날짜 형식)", example = "0분 전")
    private String createdAt;

    @Schema(description = "게시글 생성자", example = "user_001")
    private String createdBy;

    @Schema(description = "게시글이 스크랩 되었는지 여부", example = "true")
    private boolean scrapped;

    @Schema(description = "스크랩 ID (해당 게시글이 스크랩된 경우)", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapId;

    public static PostDetailResponse from(Post post) {
        PostDetailResponse dto = new PostDetailResponse();
        dto.setPostId(post.getPostId());
        dto.setBoardId(post.getBoardId());
        dto.setUserId(post.getUserId());
        dto.setNickname(post.getNickname());
        if (post.getBoard() != null) {
            dto.boardName = post.getBoard().getBoardName();
            if (post.getBoard().getCategory() != null) {
                dto.categoryName = post.getBoard().getCategory().getCategoryName();
            }else{
                dto.categoryName = post.getBoard().getCategoryName() != null ? post.getBoard().getCategoryName() : null;
            }
        }
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setViewCount(post.getViewCount());
        dto.setCommentCount(post.getCommentCount());
        // createdAt을 RelativeTimeFormatter로 포맷
        dto.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()));
        dto.setCreatedBy(post.getCreatedBy());
        dto.scrapped = post.isScrapped();
        dto.scrapId = post.getScrapId();
        return dto;
    }
}
