package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.AccountStatus;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.user.UserProfileResponse;
import com.p4th.backend.dto.response.auth.LoginResponse;
import com.p4th.backend.dto.response.auth.SignUpResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.util.IpUtil;
import com.p4th.backend.util.PassCodeUtil;
import com.p4th.backend.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    // 회원가입: 회원가입 요청 후 바로 SignUpResponse 반환
    public SignUpResponse signUp(String userId, String password, String nickname) {
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

        return new SignUpResponse(userId, passCode);
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
        if(user != null) {
            // BLOCKED 상태이고, 차단날짜가 3일 이상 경과한 경우 닉네임 재사용 가능
            if (user.getAccountStatus() == AccountStatus.BLOCKED &&
                    user.getAccountStatusChangedAt() != null &&
                    user.getAccountStatusChangedAt().plusDays(3).isBefore(LocalDateTime.now())) {
                user.setNickname("차단_" + user.getUserId());
                user.setUpdatedBy("SYSTEM");
                authMapper.updateUserNickname(user);
                return true;
            }
        }
        return user == null;
    }

    // 로그인: 회원ID와 비밀번호 확인 후 바로 LoginResponse 반환
    public LoginResponse login(String userId, String rawPassword, HttpServletRequest request) {
        String clientIp = IpUtil.extractClientIp(request);
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!PasswordUtil.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        authMapper.updateTokens(user);
        user.setLastLoginIp(clientIp);
        authMapper.updateLastLoginInfo(user);
        return new LoginResponse(accessToken, refreshToken, new UserProfileResponse(user));
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
     * 토큰 갱신: userId와 전달받은 리프레쉬 토큰을 이용하여 토큰 갱신을 수행한다.
     */
    public LoginResponse refreshTokenForMember(String userId, String refreshToken) {
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if (!jwtProvider.validateToken(refreshToken)) {
            // 리프레쉬 토큰 만료: 새 토큰 발급
            String newRefreshToken = jwtProvider.generateRefreshToken(user);
            String newAccessToken = jwtProvider.generateAccessToken(user);
            user.setRefreshToken(newRefreshToken);
            authMapper.updateTokens(user);
            return new LoginResponse(newAccessToken, newRefreshToken, new UserProfileResponse(user));
        } else {
            String newAccessToken = jwtProvider.generateAccessToken(user);
            return new LoginResponse(newAccessToken, refreshToken, new UserProfileResponse(user));
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
            // BLOCKED 상태이고, 차단날짜가 3일 이상 경과한 경우 닉네임 재사용 가능
            if (checkNickname.getAccountStatus() == AccountStatus.BLOCKED &&
                    checkNickname.getAccountStatusChangedAt() != null &&
                    checkNickname.getAccountStatusChangedAt().plusDays(3).isBefore(LocalDateTime.now())) {
                checkNickname.setNickname("차단_" + user.getUserId());
                checkNickname.setUpdatedBy("SYSTEM");
                authMapper.updateUserNickname(checkNickname);
            }
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
}
