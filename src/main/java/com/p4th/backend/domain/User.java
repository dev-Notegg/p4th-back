package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String userId;      // CHAR(36) PK
    private String loginId;     // 아이디
    private String password;    // BCrypt 해시
    private String nickname;
    private String gender;      // 'M','F' 등
    private String userType;    // 'USER','ADMIN'
    private String passCode;    // 계정 복구용
    private String reservedField;
    private String accountStatus; // 'ACTIVE','WITHDRAWN','SUSPENDED'
    private String lastLoginIp;

    private String accessToken;
    private String refreshToken;
    private String createdBy;
    private String updatedBy;
}