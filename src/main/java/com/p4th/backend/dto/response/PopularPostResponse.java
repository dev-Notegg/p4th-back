package com.p4th.backend.dto.response;

import lombok.Data;

@Data
public class PopularPostResponse {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String category; // 카테고리명
    private String boardName; // 게시판명
    private int viewCount;
    private int commentCount;
    private String imageUrl;
    private int imageCount;
    private String createdAt;
}
