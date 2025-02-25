package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.user.UserProfileResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.util.PassCodeUtil;
import com.p4th.backend.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;
    private final JwtProvider jwtProvider;

    // 회원가입: 입력받은 userId를 사용 (PK)
    public SignUpResult signUp(String userId, String password, String nickname) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(PasswordUtil.encode(password));
        String passCode = PassCodeUtil.generatePassCode();
        user.setPassCode(passCode);
        user.setNickname(nickname);
        try {
            authMapper.insertUser(user);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원가입 중 오류 발생: " + Arrays.toString(e.getStackTrace()));
        }

        return new SignUpResult(userId, passCode);
    }

    // 회원ID 중복 체크
    public boolean checkUserIdAvailable(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "아이디는 빈값일 수 없습니다.");
        }
        User user = authMapper.selectByUserId(userId);
        return user == null;
    }

    // 닉네임 중복 체크
    public boolean checkNicknameAvailable(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "닉네임은 빈값일 수 없습니다.");
        }
        User user = authMapper.selectByNickname(nickname);
        return user == null;
    }

    // 로그인: 회원ID와 비밀번호 확인
    public LoginResult login(String userId, String rawPassword, HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!PasswordUtil.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        authMapper.updateTokens(user);
        user.setLastLoginIp(clientIp);
        authMapper.updateLastLoginInfo(user);
        return new LoginResult(accessToken, refreshToken, user);
    }

    // 아이디 찾기: passCode 기준 조회 후 userId 반환
    public String findId(String passCode) {
        User user = authMapper.selectByPassCode(passCode);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getUserId();
    }

    // 비밀번호 찾기: 회원ID와 passCode가 일치하면 임시 비밀번호 발급
    public String findPassword(String userId, String passCode) {
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getPassCode() == null || !user.getPassCode().equals(passCode)) {
            throw new CustomException(ErrorCode.INVALID_PASSCODE);
        }
        String tempPassword = PassCodeUtil.generatePassCode();
        user.setPassword(PasswordUtil.encode(tempPassword));
        authMapper.updatePassword(user);
        return tempPassword;
    }

    /**
     * 토큰 갱신: 컨트롤러에서 추출한 userId와 전달받은 리프레쉬 토큰을 이용하여 토큰 갱신을 수행한다.
     */
    public LoginResult refreshTokenForMember(String userId, String refreshToken) {
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!jwtProvider.validateToken(refreshToken)) {
            // 리프레쉬 토큰 만료: 새 리프레쉬 토큰과 엑세스 토큰 발급
            String newRefreshToken = jwtProvider.generateRefreshToken(user);
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setAccessToken(newAccessToken);
            user.setRefreshToken(newRefreshToken);
            authMapper.updateTokens(user);
            return new LoginResult(newAccessToken, newRefreshToken, user);
        } else {
            // 유효한 리프레쉬 토큰: 새 엑세스 토큰 발급
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setAccessToken(newAccessToken);
            authMapper.updateTokens(user);
            return new LoginResult(newAccessToken, refreshToken, user);
        }
    }

    @Transactional
    public UserProfileResponse changeNickname(String userId, String newNickname) {
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "닉네임은 빈값일 수 없습니다.");
        }
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        //닉네임 중복 체크
        User checkNickname = authMapper.selectByNickname(newNickname);
        if(checkNickname != null){
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 닉네임 변경 후 10일 이내 재변경 불가 체크
        if (user.getNicknameChangedAt() != null) {
            long daysSinceChange = ChronoUnit.DAYS.between(user.getNicknameChangedAt(), LocalDateTime.now());
            if (daysSinceChange < 10) {
                throw new CustomException(ErrorCode.NICKNAME_CHANGE_NOT_ALLOWED);
            }
        }
        user.setNickname(newNickname);
        user.setNicknameChangedAt(LocalDateTime.now());
        authMapper.updateUserNickname(user);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse changePassword(String userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "비밀번호는 빈값일 수 없습니다.");
        }
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        authMapper.updatePassword(user);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse deleteAccount(String userId) {
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        authMapper.deleteUser(userId);
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        try {
            User user = authMapper.selectByUserId(userId);
            if (user == null) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }
            return UserProfileResponse.from(user);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "내 계정 조회 중 오류: " + Arrays.toString(e.getStackTrace()));
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

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, take the first one.
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
