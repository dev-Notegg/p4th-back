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

    // 회원가입: 아이디, 비밀번호, 닉네임 받아서 저장 후 패쓰코드 생성 및 반환
    public SignUpResult signUp(User user) {
        user.setUserId(UUID.randomUUID().toString());
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        String passCode = PassCodeUtil.generatePassCode();
        user.setPassCode(passCode);
        userMapper.insertUser(user);
        return new SignUpResult(user.getLoginId(), passCode);
    }

    // 아이디 중복 체크: 해당 loginId가 이미 존재하면 false, 아니면 true 반환
    public boolean checkLoginIdAvailable(String loginId) {
        User user = userMapper.selectByLoginId(loginId);
        return user == null;
    }

    // 닉네임 중복 체크: 해당 nickname이 이미 존재하면 false, 아니면 true 반환
    public boolean checkNicknameAvailable(String nickname) {
        User user = userMapper.selectByNickname(nickname);
        return user == null;
    }

    // 로그인: 아이디와 비밀번호로 인증 후 토큰 발급
    public User login(String loginId, String rawPassword, String clientIp) {
        User user = userMapper.selectByLoginId(loginId);
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
        // 보안을 위해 비밀번호 및 패쓰코드 제거
        user.setPassword(null);
        user.setPassCode(null);
        return user;
    }

    // 아이디 찾기: 패쓰코드로 사용자 조회 후 로그인 아이디 반환
    public String findId(String passCode) {
        User user = userMapper.selectByPassCode(passCode);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getLoginId();
    }

    // 비밀번호 찾기: 아이디와 패쓰코드가 일치하면 임시 비밀번호 발급, DB 업데이트 후 임시 비밀번호 반환
    public String findPassword(String loginId, String passCode) {
        User user = userMapper.selectByLoginId(loginId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.getPassCode().equals(passCode)) {
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
    public RefreshTokenResult refreshTokenForMember(String memberId, String refreshToken) {
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
            return new RefreshTokenResult(newAccessToken, newRefreshToken, user);
        } else {
            // 유효한 리프레쉬 토큰: 새 엑세스 토큰 발급
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setAccessToken(newAccessToken);
            userMapper.updateTokens(user);
            return new RefreshTokenResult(newAccessToken, refreshToken, user);
        }
    }

    @Getter
    public static class SignUpResult {
        private final String loginId;
        private final String passCode;
        public SignUpResult(String loginId, String passCode) {
            this.loginId = loginId;
            this.passCode = passCode;
        }
    }

    @Getter
    public static class RefreshTokenResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        public RefreshTokenResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}
