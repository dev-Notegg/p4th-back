package com.p4th.backend.dto;

import com.p4th.backend.domain.Post;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class PostListDto {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String category;    // 게시판의 카테고리명
    private String boardName;   // 게시판명
    private int viewCount;
    private int commentCount;
    private String imageUrl;
    private int imageCount;
    private String createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Post 엔티티에서 필요한 필드를 추출하여 DTO로 변환한다.
     */
    public static PostListDto from(Post post) {
        PostListDto dto = new PostListDto();
        dto.setPostId(post.getPostId());
        dto.setBoardId(post.getBoardId());
        dto.setUserId(post.getUserId());
        dto.setNickname(post.getNickname());
        dto.setTitle(post.getTitle());
        // 연관관계가 설정되어 있다면 Board와 Category 정보를 가져온다.
        if (post.getBoard() != null) {
            dto.setBoardName(post.getBoard().getBoardName());
            if (post.getBoard().getCategory() != null) {
                dto.setCategory(post.getBoard().getCategory().getCategoryName());
            }
        }
        dto.setViewCount(post.getViewCount());
        dto.setCommentCount(post.getCommentCount());
        // 첨부파일이 존재한다면 첫 번째 첨부파일 URL과 첨부파일 개수를 설정한다.
        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            dto.setImageUrl(post.getAttachments().get(0).getFileUrl());
            dto.setImageCount(post.getAttachments().size());
        } else {
            dto.setImageUrl(null);
            dto.setImageCount(0);
        }
        if (post.getCreatedAt() != null) {
            dto.setCreatedAt(post.getCreatedAt().format(formatter));
        }
        return dto;
    }
}
