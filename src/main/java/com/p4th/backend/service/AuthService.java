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

    // 회원가입: 입력받은 userId를 사용 (PK)
    public SignUpResult signUp(User user) {
        user.setUserId(user.getUserId());
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        String passCode = PassCodeUtil.generatePassCode();
        user.setPassCode(passCode);
        userMapper.insertUser(user);
        return new SignUpResult(user.getUserId(), passCode);
    }

    // 회원ID 중복 체크
    public boolean checkUserIdAvailable(String userId) {
        User user = userMapper.selectByUserId(userId);
        return user == null;
    }

    // 닉네임 중복 체크
    public boolean checkNicknameAvailable(String nickname) {
        User user = userMapper.selectByNickname(nickname);
        return user == null;
    }

    // 로그인: 회원ID와 비밀번호 확인
    public LoginResult login(String userId, String rawPassword, String clientIp) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!PasswordUtil.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        userMapper.updateTokens(user);
        user.setLastLoginIp(clientIp);
        userMapper.updateLastLoginInfo(user);
        return new LoginResult(user.getAccessToken(), user.getRefreshToken(), user);
    }

    // 아이디 찾기: passCode 기준 조회 후 userId 반환
    public String findId(String passCode) {
        User user = userMapper.selectByPassCode(passCode);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getUserId();
    }

    // 비밀번호 찾기: 회원ID와 passCode가 일치하면 임시 비밀번호 발급
    public String findPassword(String userId, String passCode) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getPassCode() == null || !user.getPassCode().equals(passCode)) {
            throw new CustomException(ErrorCode.INVALID_PASSCODE);
        }
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(PasswordUtil.encode(tempPassword));
        userMapper.updatePassword(user);
        return tempPassword;
    }

    /**
     * 토큰 갱신: 컨트롤러에서 추출한 memberId와 전달받은 리프레쉬 토큰을 이용하여 토큰 갱신을 수행한다.
     */
    public LoginResult refreshTokenForMember(String memberId, String refreshToken) {
        User user = userMapper.selectByUserId(memberId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!jwtProvider.validateToken(refreshToken)) {
            // 리프레쉬 토큰 만료: 새 리프레쉬 토큰과 엑세스 토큰 발급
            String newRefreshToken = jwtProvider.generateRefreshToken(user.getUserId());
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setAccessToken(newAccessToken);
            user.setRefreshToken(newRefreshToken);
            userMapper.updateTokens(user);
            return new LoginResult(newAccessToken, newRefreshToken, user);
        } else {
            // 유효한 리프레쉬 토큰: 새 엑세스 토큰 발급
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setAccessToken(newAccessToken);
            userMapper.updateTokens(user);
            return new LoginResult(newAccessToken, refreshToken, user);
        }
    }

    @Getter
    public static class SignUpResult {
        private final String userId;
        private final String passCode;
        public SignUpResult(String userId, String passCode) {
            this.userId = userId;
            this.passCode = passCode;
        }
    }

    @Getter
    public static class LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        public LoginResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}
