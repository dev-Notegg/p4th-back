package com.p4th.backend.dto.response.post;

import lombok.Data;

@Data
public class CreatePostResponse {
    private String postId;
    public CreatePostResponse(String postId) {
        this.postId = postId;
    }
}