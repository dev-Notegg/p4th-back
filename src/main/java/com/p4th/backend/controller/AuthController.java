package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.SignupRequestDto;
import com.p4th.backend.service.AuthService;
import com.p4th.backend.service.AuthService.LoginResult;
import com.p4th.backend.service.AuthService.SignUpResult;
import com.p4th.backend.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "계정/토큰 관련 API")
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입", description = "회원ID, 비밀번호, 닉네임을 받아 회원가입을 진행하며, 가입 완료 후 회원ID와 패쓰코드를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 중복된 회원ID/닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Parameter(name = "SignupRequestDto", description = "회원가입 요청 DTO (userId, password, nickname)", required = true)
            @RequestBody SignupRequestDto request) {
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        SignUpResult result = authService.signUp(user);
        SignUpResponse response = new SignUpResponse();
        response.setUserId(result.getUserId());
        response.setPassCode(result.getPassCode());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "회원ID 중복 확인", description = "전달된 회원ID를 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-user-id")
    public ResponseEntity<CheckResponse> checkUserId(
            @Parameter(name = "userId", description = "확인할 회원ID", required = true)
            @RequestParam String userId) {
        boolean available = authService.checkUserIdAvailable(userId);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "전달된 nickname을 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<CheckResponse> checkNickname(
            @Parameter(name = "nickname", description = "확인할 닉네임", required = true)
            @RequestParam String nickname) {
        boolean available = authService.checkNicknameAvailable(nickname);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "로그인", description = "로그인 시, 요청 본문에 회원ID와 비밀번호를 담아 전송하며, 성공 시 엑세스 토큰, 리프레쉬 토큰 및 사용자 정보를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "사용자를 찾을 수 없거나 비밀번호가 틀린 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(name = "LoginRequest", description = "로그인 요청 DTO (userId, password)", required = true)
            @RequestBody LoginRequest requestDto,
            HttpServletRequest request) {
        LoginResult result = authService.login(requestDto.getUserId(), requestDto.getPassword(), request.getRemoteAddr());
        LoginResponse response = new LoginResponse();
        response.setAccessToken(result.getAccessToken());
        response.setRefreshToken(result.getRefreshToken());
        response.setUser(new UserDto(result.getUser()));
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "아이디 찾기", description = "패쓰코드를 입력받아 해당 사용자의 회원ID를 반환하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공",
                    content = @Content(schema = @Schema(implementation = FindIdResponse.class))),
            @ApiResponse(responseCode = "400", description = "패쓰코드에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(
            @Parameter(name = "passCode", description = "패쓰코드", required = true)
            @RequestBody FindIdRequest request) {
        String userId = authService.findId(request.getPassCode());
        FindIdResponse response = new FindIdResponse();
        response.setUserId(userId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "비밀번호 찾기", description = "회원ID와 패쓰코드를 입력받아 임시 비밀번호를 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공",
                    content = @Content(schema = @Schema(implementation = FindPasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 정보에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-password")
    public ResponseEntity<FindPasswordResponse> findPassword(
            @Parameter(name = "FindPasswordRequest", description = "비밀번호 찾기 요청 DTO (userId, passCode)", required = true)
            @RequestBody FindPasswordRequest request) {
        String tempPassword = authService.findPassword(request.getUserId(), request.getPassCode());
        FindPasswordResponse response = new FindPasswordResponse();
        response.setPassword(tempPassword);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "토큰 갱신", description = "리프레쉬 토큰을 사용하여 새로운 엑세스 토큰(및 필요시 리프레쉬 토큰)을 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "해당 회원을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "리프레쉬 토큰이 유효하지 않은 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/v1/token")
    public ResponseEntity<LoginResponse> updateToken(
            @Parameter(name = "RefreshTokenRequest", description = "토큰 갱신 요청 DTO (refreshToken)", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {
        String userId = jwtProvider.getUserIdFromToken(request.getRefreshToken());
        LoginResult result = authService.refreshTokenForMember(userId, request.getRefreshToken());
        LoginResponse response = new LoginResponse();
        response.setAccessToken(result.getAccessToken());
        response.setRefreshToken(result.getRefreshToken());
        response.setUser(new UserDto(result.getUser()));
        return ResponseEntity.ok().body(response);
    }

    // --------------------- 내부 DTO 클래스 ---------------------

    @Data
    public static class SignUpResponse {
        private String userId;
        private String passCode;
    }

    @Data
    public static class LoginRequest {
        private String userId;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private UserDto user;
    }

    @Data
    public static class FindIdRequest {
        private String passCode;
    }

    @Data
    public static class FindIdResponse {
        private String userId;
    }

    @Data
    public static class FindPasswordRequest {
        private String userId;
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
    public static class UserDto {
        private String userId;
        private String nickname;
        private int membershipLevel;
        private int adminRole;

        public UserDto(User user) {
            this.userId = user.getUserId();
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
