package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.User;
import com.p4th.backend.service.AuthService;
import com.p4th.backend.service.AuthService.RefreshTokenResult;
import com.p4th.backend.service.AuthService.SignUpResult;
import com.p4th.backend.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입: 아이디, 비밀번호, 닉네임을 받아 회원가입 후 로그인ID와 패쓰코드를 반환한다.
     */
    @Operation(summary = "회원가입", description = "아이디, 비밀번호, 닉네임을 받아 회원가입을 진행하며, 가입 완료 후 로그인ID와 패쓰코드를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 중복된 아이디/닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public SignUpResponse signUp(@RequestBody User user) {
        SignUpResult result = authService.signUp(user);
        SignUpResponse response = new SignUpResponse();
        response.setLoginId(result.getLoginId());
        response.setPassCode(result.getPassCode());
        return response;
    }

    // 아이디 중복 확인 API
    @Operation(summary = "아이디 중복 확인", description = "전달된 loginId를 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-login-id")
    public CheckResponse checkLoginId(@RequestParam String loginId) {
        boolean available = authService.checkLoginIdAvailable(loginId);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return response;
    }

    // 닉네임 중복 확인 API
    @Operation(summary = "닉네임 중복 확인", description = "전달된 nickname을 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-nickname")
    public CheckResponse checkNickname(@RequestParam String nickname) {
        boolean available = authService.checkNicknameAvailable(nickname);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return response;
    }

    /**
     * 로그인: 아이디와 비밀번호를 요청 본문(JSON)으로 받아 로그인 후 토큰 및 사용자 정보를 반환한다.
     */
    @Operation(summary = "로그인", description = "로그인 시, 요청 본문에 로그인ID와 비밀번호를 담아 전송하며, 성공 시 엑세스 토큰, 리프레쉬 토큰 및 사용자 정보를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "사용자를 찾을 수 없거나 비밀번호가 틀린 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest requestDto,
                               HttpServletRequest request) {
        User user = authService.login(requestDto.getLoginId(), requestDto.getPassword(), request.getRemoteAddr());
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setLoginId(user.getLoginId());
        response.setAccessToken(user.getAccessToken());
        response.setRefreshToken(user.getRefreshToken());
        return response;
    }

    /**
     * 아이디 찾기: 패쓰코드를 받아 해당 사용자의 로그인 아이디를 반환한다.
     */
    @Operation(summary = "아이디 찾기", description = "패쓰코드를 입력받아 해당 사용자의 로그인 아이디를 반환하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공",
                    content = @Content(schema = @Schema(implementation = FindIdResponse.class))),
            @ApiResponse(responseCode = "400", description = "패쓰코드에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-id")
    public FindIdResponse findId(@RequestBody FindIdRequest request) {
        String loginId = authService.findId(request.getPassCode());
        FindIdResponse response = new FindIdResponse();
        response.setLoginId(loginId);
        return response;
    }

    /**
     * 비밀번호 찾기: 아이디와 패쓰코드를 입력받아 임시 비밀번호를 발급하는 API이다.
     */
    @Operation(summary = "비밀번호 찾기", description = "아이디와 패쓰코드를 입력받아 임시 비밀번호를 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공",
                    content = @Content(schema = @Schema(implementation = FindPasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 정보에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-password")
    public FindPasswordResponse findPassword(@RequestBody FindPasswordRequest request) {
        String tempPassword = authService.findPassword(request.getLoginId(), request.getPassCode());
        FindPasswordResponse response = new FindPasswordResponse();
        response.setPassword(tempPassword);
        return response;
    }

    /**
     * 토큰 갱신: 리프레쉬 토큰 페이로드에서 회원 ID를 추출하여 새로운 엑세스 토큰(및 필요한 경우 리프레쉬 토큰)을 발급한다.
     */
    @Operation(summary = "토큰 갱신", description = "로그인 시 받은 리프레쉬 토큰을 페이로드에서 추출한 회원 ID를 사용하여 새로운 엑세스 토큰과 (필요 시) 리프레쉬 토큰을 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "해당 회원을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "리프레쉬 토큰이 유효하지 않은 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/v1/token")
    public ResponseEntity<RefreshTokenResponse> updateToken(@Valid @RequestBody RefreshTokenRequest request) {
        // 리프레쉬 토큰의 페이로드에서 회원 ID 추출
        String memberId = jwtProvider.getUserIdFromToken(request.getRefreshToken());
        RefreshTokenResult result = authService.refreshTokenForMember(memberId, request.getRefreshToken());
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(result.getAccessToken());
        response.setRefreshToken(result.getRefreshToken());
        response.setUser(new UserDto(result.getUser()));
        return ResponseEntity.ok(response);
    }

    // --------------------- 내부 DTO 클래스 ---------------------

    @Data
    public static class SignUpResponse {
        private String loginId;
        private String passCode;
    }

    @Data
    public static class LoginRequest {
        private String loginId;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String userId;
        private String loginId;
        private String accessToken;
        private String refreshToken;
    }

    @Data
    public static class FindIdRequest {
        private String passCode;
    }

    @Data
    public static class FindIdResponse {
        private String loginId;
    }

    @Data
    public static class FindPasswordRequest {
        private String loginId;
        private String passCode;
    }

    @Data
    public static class FindPasswordResponse {
        private String password;
    }

    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Data
    public static class RefreshTokenResponse {
        private String accessToken;
        private String refreshToken;
        private UserDto user;
    }

    @Data
    public static class UserDto {
        private String userId;
        private String loginId;
        private String nickname;
        private int membershipLevel;
        private int adminRole;

        public UserDto(User user) {
            this.userId = user.getUserId();
            this.loginId = user.getLoginId();
            this.nickname = user.getNickname();
            this.membershipLevel = user.getMembershipLevel();
            this.adminRole = user.getAdminRole();
        }
    }

    @Data
    public static class CheckResponse {
        private boolean available;
    }
}
