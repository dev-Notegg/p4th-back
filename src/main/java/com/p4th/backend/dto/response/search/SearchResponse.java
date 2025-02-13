package com.p4th.backend.dto.response.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private List<SearchResult> results;
    private int total;

    @Data
    public static class SearchResult {
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
    }
}
