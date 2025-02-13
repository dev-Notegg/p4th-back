package com.p4th.backend.dto.response.post;

import lombok.Data;

@Data
public class DeletePostResponse {
    private boolean deleted;
    public DeletePostResponse(boolean deleted) {
        this.deleted = deleted;
    }
}