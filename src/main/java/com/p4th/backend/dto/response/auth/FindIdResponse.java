package com.p4th.backend.dto.response.auth;

import lombok.Data;

@Data
public class FindIdResponse {
    private String userId;
    public FindIdResponse(String userId) {
        this.userId = userId;
    }
}
