package com.p4th.backend.dto.response.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.util.HtmlContentUtils;
import lombok.Data;
import java.time.format.DateTimeFormatter;

@Data
public class PostListResponse {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String categoryName; // 게시판의 카테고리명
    private String boardName;    // 게시판명
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;
    private PostStatus status;
    private int viewCount;
    private int commentCount;
    private String imageUrl;    // 썸네일 이미지 URL
    private int imageCount;     // HTML 내 이미지 태그 개수
    private String createdAt;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostListResponse from(Post post) {
        PostListResponse dto = new PostListResponse();
        dto.postId = post.getPostId();
        dto.boardId = post.getBoardId();
        dto.userId = post.getUserId();
        if(post.getUser() != null) {
            dto.nickname = post.getUser().getNickname();
        }else{
            dto.nickname = post.getNickname() != null ? post.getNickname() : null;
        }
        dto.title = HtmlContentUtils.extractText(post.getTitle(), 30);
        if (post.getBoard() != null) {
            dto.boardName = post.getBoard().getBoardName();
            if (post.getBoard().getCategory() != null) {
                dto.categoryName = post.getBoard().getCategory().getCategoryName();
            }else{
                dto.categoryName = post.getBoard().getCategoryName() != null ? post.getBoard().getCategoryName() : null;
            }
        }
        dto.status = post.getStatus();
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();

        // HTML 태그 제거 후 순수 텍스트 추출 (최대 50자)
        dto.setContent(HtmlContentUtils.extractPlainText(post.getContent(), 100));

        // HTML 내 이미지 태그 처리
        dto.imageCount = HtmlImageUtils.countInlineImages(post.getContent());
        dto.imageUrl = HtmlImageUtils.extractFirstImageUrl(post.getContent());

        if (post.getCreatedAt() != null) {
            dto.createdAt = RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt());
        }
        return dto;
    }








}
