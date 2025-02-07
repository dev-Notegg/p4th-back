package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.User;
import com.p4th.backend.common.CommonResponse;
import com.p4th.backend.dto.SignupRequestDto;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입", description = "로그인ID, 비밀번호, 닉네임을 받아 회원가입을 진행하며, 가입 완료 후 로그인ID와 패쓰코드를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 중복된 아이디/닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public CommonResponse<SignUpResponse> signUp(@RequestBody SignupRequestDto request) {
        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        SignUpResult result = authService.signUp(user);
        SignUpResponse response = new SignUpResponse();
        response.setLoginId(result.getLoginId());
        response.setPassCode(result.getPassCode());
        return CommonResponse.success(response);
    }

    @Operation(summary = "아이디 중복 확인", description = "전달된 loginId를 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-login-id")
    public CommonResponse<CheckResponse> checkLoginId(@RequestParam String loginId) {
        boolean available = authService.checkLoginIdAvailable(loginId);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return CommonResponse.success(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "전달된 nickname을 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class)))
    })
    @GetMapping("/check-nickname")
    public CommonResponse<CheckResponse> checkNickname(@RequestParam String nickname) {
        boolean available = authService.checkNicknameAvailable(nickname);
        CheckResponse response = new CheckResponse();
        response.setAvailable(available);
        return CommonResponse.success(response);
    }

    @Operation(summary = "로그인", description = "로그인 시, 요청 본문에 로그인ID와 비밀번호를 담아 전송하며, 성공 시 엑세스 토큰, 리프레쉬 토큰 및 사용자 정보를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "사용자를 찾을 수 없거나 비밀번호가 틀린 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@RequestBody LoginRequest requestDto,
                                               HttpServletRequest request) {
        User user = authService.login(requestDto.getLoginId(), requestDto.getPassword(), request.getRemoteAddr());
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setLoginId(user.getLoginId());
        response.setAccessToken(user.getAccessToken());
        response.setRefreshToken(user.getRefreshToken());
        return CommonResponse.success(response);
    }

    @Operation(summary = "아이디 찾기", description = "패쓰코드를 입력받아 해당 사용자의 로그인 아이디를 반환하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공",
                    content = @Content(schema = @Schema(implementation = FindIdResponse.class))),
            @ApiResponse(responseCode = "400", description = "패쓰코드에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-id")
    public CommonResponse<FindIdResponse> findId(@RequestBody FindIdRequest request) {
        String loginId = authService.findId(request.getPassCode());
        FindIdResponse response = new FindIdResponse();
        response.setLoginId(loginId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "비밀번호 찾기", description = "아이디와 패쓰코드를 입력받아 임시 비밀번호를 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공",
                    content = @Content(schema = @Schema(implementation = FindPasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 정보에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-password")
    public CommonResponse<FindPasswordResponse> findPassword(@RequestBody FindPasswordRequest request) {
        String tempPassword = authService.findPassword(request.getLoginId(), request.getPassCode());
        FindPasswordResponse response = new FindPasswordResponse();
        response.setPassword(tempPassword);
        return CommonResponse.success(response);
    }

    @Operation(summary = "토큰 갱신", description = "리프레쉬 토큰을 사용하여 새로운 엑세스 토큰(및 필요시 리프레쉬 토큰)을 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "해당 회원을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "리프레쉬 토큰이 유효하지 않은 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/v1/token")
    public CommonResponse<RefreshTokenResponse> updateToken(@Valid @RequestBody RefreshTokenRequest request) {
        String memberId = jwtProvider.getUserIdFromToken(request.getRefreshToken());
        RefreshTokenResult result = authService.refreshTokenForMember(memberId, request.getRefreshToken());
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(result.getAccessToken());
        response.setRefreshToken(result.getRefreshToken());
        response.setUser(new UserDto(result.getUser()));
        return CommonResponse.success(response);
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
