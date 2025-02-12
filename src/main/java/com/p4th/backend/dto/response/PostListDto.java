package com.p4th.backend.dto.response;

import com.p4th.backend.domain.Post;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class PostListDto {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String category;
    private String boardName;
    private int viewCount;
    private int commentCount;
    private String imageUrl;
    private int imageCount;
    private String createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostListDto from(Post post) {
        PostListDto dto = new PostListDto();
        dto.postId = post.getPostId();
        dto.boardId = post.getBoardId();
        dto.userId = post.getUserId();
        dto.nickname = post.getUser() != null ? post.getUser().getNickname() : "";
        dto.title = post.getTitle();
        if (post.getBoard() != null) {
            dto.boardName = post.getBoard().getBoardName();
            if (post.getBoard().getCategory() != null) {
                dto.category = post.getBoard().getCategory().getCategoryName();
            }
        }
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();
        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            dto.imageUrl = post.getAttachments().get(0).getFileUrl();
            dto.imageCount = post.getAttachments().size();
        } else {
            dto.imageUrl = null;
            dto.imageCount = 0;
        }
        if (post.getCreatedAt() != null) {
            dto.createdAt = post.getCreatedAt().format(formatter);
        }
        return dto;
    }
}
