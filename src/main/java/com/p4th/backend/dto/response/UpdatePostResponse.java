package com.p4th.backend.dto.response;

import lombok.Data;

@Data
public class UpdatePostResponse {
    private String postId;
    public UpdatePostResponse(String postId) {
        this.postId = postId;
    }
}