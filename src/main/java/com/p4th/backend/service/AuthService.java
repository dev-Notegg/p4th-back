package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.User;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.util.PassCodeUtil;
import com.p4th.backend.util.PasswordUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    // 회원가입
    public SignUpResult signUp(User user) {
        // 중복체크 필요 시 throw new CustomException(ErrorCode.PHONE_DUPLICATED) 등
        user.setUserId(UUID.randomUUID().toString());

        // 비밀번호 해시
        user.setPassword(PasswordUtil.encode(user.getPassword()));

        // 'USER'
        user.setUserType("USER");
        user.setAccountStatus("ACTIVE");

        // 패쓰코드 생성
        String passCode = PassCodeUtil.generatePassCode();
        user.setPassCode(passCode);

        // Insert
        userMapper.insertUser(user);

        // 발급된 passCode
        return new SignUpResult(passCode);
    }

    // 로그인
    public User login(String loginId, String rawPassword, String clientIp) {
        User user = userMapper.selectByLoginId(loginId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!PasswordUtil.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // JWT
        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        userMapper.updateTokens(user);

        user.setLastLoginIp(clientIp);
        userMapper.updateLastLoginInfo(user);

        // 비번, 패쓰코드 노출 X
        user.setPassword(null);
        user.setPassCode(null);

        return user;
    }

    // 회원가입 응답 DTO
    @Getter
    public static class SignUpResult {
        private final String passCode; // 발급된 패쓰코드

        public SignUpResult(String passCode) {
            this.passCode = passCode;
        }

    }
}
