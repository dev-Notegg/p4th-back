package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_info")
public class User {
    @Id
    private String userId;         // PK (회원ID)
    private String loginId;        // 아이디
    private String password;       // BCrypt 해시
    private String nickname;
    private String passCode;       // 계정 복구용
    private int membershipLevel;   // 회원 등급
    private int adminRole;         // 관리자 등급
    private String accountStatus;  // 'ACTIVE','WITHDRAWN','SUSPENDED'
    private String lastLoginIp;
    private String accessToken;
    private String refreshToken;
    private String createdBy;
    private String updatedBy;
}
