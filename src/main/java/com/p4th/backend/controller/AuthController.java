package com.p4th.backend.controller;

import com.p4th.backend.domain.User;
import com.p4th.backend.service.AuthService;
import com.p4th.backend.service.AuthService.SignUpResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public SignUpResponse signUp(@RequestBody User user) {
        SignUpResult signUpResult = authService.signUp(user);

        // Controller 응답 DTO
        SignUpResponse resp = new SignUpResponse();
        resp.setPassCode(signUpResult.getPassCode());
        return resp; // 200 OK
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestParam String loginId,
                               @RequestParam String password,
                               HttpServletRequest request) {
        // 성공 시 User 반환
        var user = authService.login(loginId, password, request.getRemoteAddr());

        // 응답
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getUserId());
        resp.setLoginId(user.getLoginId());
        resp.setAccessToken(user.getAccessToken());
        resp.setRefreshToken(user.getRefreshToken());
        return resp; // 200 OK
    }

    // 회원가입 응답 DTO
    @Data
    public static class SignUpResponse {
        private String passCode;   // 발급된 패쓰코드
    }

    // 로그인 응답 DTO
    @Data
    public static class LoginResponse {
        private String userId;
        private String loginId;
        private String accessToken;
        private String refreshToken;
    }
}
