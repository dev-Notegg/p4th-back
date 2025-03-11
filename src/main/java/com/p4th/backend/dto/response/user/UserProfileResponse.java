package com.p4th.backend.dto.response.user;

import com.p4th.backend.domain.User;
import com.p4th.backend.domain.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "내 계정 조회 응답 DTO")
public class UserProfileResponse {
    @Schema(description = "사용자 ID", example = "id1234")
    private String userId;

    @Schema(description = "닉네임", example = "닉네임")
    private String nickname;

    @Schema(description = "회원 등급", example = "1")
    private Integer membershipLevel;

    @Schema(description = "관리자 권한", example = "0")
    private Integer adminRole;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "패쓰코드", example = "abcdef12345")
    private String passCode;

    @Schema(description = "가입일", example = "2025-10-01")
    private String joinDate;

    public static UserProfileResponse from(User user) {
        UserProfileResponse dto = new UserProfileResponse(user);
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getNickname());
        dto.setMembershipLevel(user.getMembershipLevel());
        dto.setAdminRole(user.getAdminRole());
        dto.setAccountStatus(user.getAccountStatus());
        return dto;
    }

    public static UserProfileResponse fromWithExtraInfo(User user) {
        UserProfileResponse dto = from(user);
        dto.setPassCode(user.getPassCode());
        if (user.getCreatedAt() != null) {
            dto.setJoinDate(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return dto;
    }

    public UserProfileResponse(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.membershipLevel = user.getMembershipLevel();
        this.adminRole = user.getAdminRole();
    }
}
