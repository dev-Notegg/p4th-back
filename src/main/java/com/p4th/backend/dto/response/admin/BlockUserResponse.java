package com.p4th.backend.dto.response.admin;

import com.p4th.backend.domain.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.p4th.backend.domain.User;

import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "회원 차단 관리 응답 DTO")
public class BlockUserResponse {

    @Schema(description = "IP")
    private String ipAddress;

    @Schema(description = "회원 ID")
    private String userId;
    
    @Schema(description = "닉네임")
    private String nickname;
    
    @Schema(description = "패쓰코드")
    private String passCode;
    
    @Schema(description = "가입날짜", example = "2025-10-01")
    private String joinDate;

    @Schema(description = "차단날짜", example = "2025-10-01")
    private String blockedDate;

    @Schema(description = "차단 상태", example = "BLOCKED")
    private AccountStatus accountStatus;
    
    public static BlockUserResponse from(User user) {
        BlockUserResponse dto = new BlockUserResponse();
        dto.setIpAddress(user.getLastLoginIp());
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getNickname());
        dto.setPassCode(user.getPassCode());
        if (user.getCreatedAt() != null) {
            dto.setJoinDate(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (user.getAccountStatus().equals(AccountStatus.BLOCKED) && user.getAccountStatusChangedAt() != null) {
            dto.setBlockedDate(user.getAccountStatusChangedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        dto.setAccountStatus(user.getAccountStatus());
        return dto;
    }
}
