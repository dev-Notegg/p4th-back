package com.p4th.backend.dto.response.auth;

import com.p4th.backend.domain.User;
import lombok.Data;

@Data
public class UserResponse {
    private String userId;
    private String nickname;
    private int membershipLevel;
    private int adminRole;

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.membershipLevel = user.getMembershipLevel();
        this.adminRole = user.getAdminRole();
    }
}