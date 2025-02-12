package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_info")
public class User {
    @Id
    private String userId;         // PK (회원ID)
    private String password;       // BCrypt 해시
    private LocalDateTime passwordChangedAt;
    private String passCode;       // 계정 복구용
    private String nickname;
    private int membershipLevel;   // 회원 등급
    private int adminRole;         // 관리자 등급
    private int warning_count;     //누적신고 처리횟수
    private String accountStatus;  // 'ACTIVE','WITHDRAWN','SUSPENDED'
    private LocalDateTime accountStatusChangedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private String accessToken;
    private String refreshToken;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
