package com.p4th.backend.dto.response.scrap;

import lombok.Data;

import java.util.List;

@Data
public class UserScrapResponse {
    private String scrapFolderId;
    private List<ScrapPostListResponse> scrapPosts;
}
