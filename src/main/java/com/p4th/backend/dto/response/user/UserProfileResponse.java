package com.p4th.backend.dto.response.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.p4th.backend.domain.User;
import com.p4th.backend.domain.AccountStatus;
import com.p4th.backend.util.RelativeTimeFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "내 계정 조회 응답 DTO")
public class UserProfileResponse {
    @Schema(description = "사용자 ID", example = "id1234")
    private String userId;

    @Schema(description = "닉네임", example = "닉네임")
    private String nickname;

    @Schema(description = "회원 등급", example = "1")
    private int membershipLevel;

    @Schema(description = "관리자 권한", example = "0")
    private int adminRole;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private AccountStatus accountStatus;

    @JsonIgnore
    private String createdAt;

    public static UserProfileResponse from(User user) {
        UserProfileResponse dto = new UserProfileResponse(user);
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getNickname());
        dto.setMembershipLevel(user.getMembershipLevel());
        dto.setAdminRole(user.getAdminRole());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(user.getCreatedAt()));
        return dto;
    }

    public UserProfileResponse(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.membershipLevel = user.getMembershipLevel();
        this.adminRole = user.getAdminRole();
    }
}
