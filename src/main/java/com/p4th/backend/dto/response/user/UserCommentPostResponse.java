package com.p4th.backend.dto.response.user;

import com.p4th.backend.util.HtmlContentUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.p4th.backend.domain.Post;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;

import java.util.List;

@Data
@Schema(description = "내가 쓴 댓글이 포함된 게시글 응답 DTO")
public class UserCommentPostResponse {
    @Schema(description = "게시글 ID")
    private String postId;

    @Schema(description = "게시판 ID")
    private String boardId;

    @Schema(description = "게시글 작성자 ID")
    private String userId;

    @Schema(description = "게시글 작성자 닉네임")
    private String nickname;

    @Schema(description = "카테고리명")
    private String categoryName;

    @Schema(description = "게시판명")
    private String boardName;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "조회수")
    private int viewCount;

    @Schema(description = "댓글 수")
    private int commentCount;

    @Schema(description = "썸네일 이미지 URL")
    private String imageUrl;

    @Schema(description = "이미지 개수")
    private int imageCount;

    @Schema(description = "게시글 작성일 (상대 시간 형식)")
    private String createdAt;

    @Schema(description = "내가 쓴 댓글 목록")
    private List<UserCommentResponse> comments;

    public static UserCommentPostResponse from(Post post) {
        UserCommentPostResponse dto = new UserCommentPostResponse();
        dto.setPostId(post.getPostId());
        dto.setBoardId(post.getBoardId());
        dto.setUserId(post.getUserId());
        dto.setNickname(post.getNickname() != null ? post.getNickname() : "");
        dto.setTitle(HtmlContentUtils.extractText(post.getTitle(), 30));
        if (post.getBoard() != null) {
            dto.setBoardName(post.getBoard().getBoardName());
            if (post.getBoard().getCategory() != null) {
                dto.setCategoryName(post.getBoard().getCategory().getCategoryName());
            }else{
                dto.setCategoryName(post.getBoard().getCategoryName() != null ? post.getBoard().getCategoryName() : null);
            }
        }
        dto.setViewCount(post.getViewCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setImageUrl(HtmlImageUtils.extractFirstImageUrl(post.getContent()));
        dto.setImageCount(HtmlImageUtils.countInlineImages(post.getContent()));
        dto.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()));
        return dto;
    }
}
