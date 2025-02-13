package com.p4th.backend.dto.response.auth;

import lombok.Data;

@Data
public class SignUpResponse {
    private String userId;
    private String passCode;
    public SignUpResponse(String userId, String passCode) {
        this.userId = userId;
        this.passCode = passCode;
    }
}
