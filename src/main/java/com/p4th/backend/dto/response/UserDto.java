package com.p4th.backend.dto.response;

import com.p4th.backend.domain.User;
import lombok.Data;

@Data
public class UserDto {
    private String userId;
    private String nickname;
    private int membershipLevel;
    private int adminRole;

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.membershipLevel = user.getMembershipLevel();
        this.adminRole = user.getAdminRole();
    }
}