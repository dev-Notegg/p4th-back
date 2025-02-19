package com.p4th.backend.dto.response.scrap;

import com.p4th.backend.dto.response.post.PostListResponse;
import lombok.Data;

import java.util.List;

@Data
public class UserScrapResponse {
    private String scrapFolderId;
    private List<PostListResponse> content;
}
