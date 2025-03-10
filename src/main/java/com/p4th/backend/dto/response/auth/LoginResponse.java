package com.p4th.backend.dto.response.auth;

import com.p4th.backend.dto.response.user.UserProfileResponse;
import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserProfileResponse user;
    public LoginResponse(String accessToken, String refreshToken, UserProfileResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
