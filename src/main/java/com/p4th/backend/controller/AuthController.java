package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.dto.request.SignupRequest;
import com.p4th.backend.dto.response.auth.UserResponse;
import com.p4th.backend.dto.request.FindIdRequest;
import com.p4th.backend.dto.request.FindPasswordRequest;
import com.p4th.backend.dto.request.LoginRequest;
import com.p4th.backend.dto.request.RefreshTokenRequest;
import com.p4th.backend.dto.response.CheckResponse;
import com.p4th.backend.dto.response.auth.FindIdResponse;
import com.p4th.backend.dto.response.auth.FindPasswordResponse;
import com.p4th.backend.dto.response.auth.LoginResponse;
import com.p4th.backend.dto.response.auth.SignUpResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "인증 및 토큰 관련 API")
@RestController
@RequestMapping(value = "/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입", description = "회원ID, 비밀번호, 닉네임을 받아 회원가입을 진행하며, 가입 완료 후 회원ID와 패쓰코드를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "500", description = "회원가입 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Parameter(name = "SignupRequestDto", description = "회원가입 요청 DTO (userId, password, nickname)", required = true)
            @RequestBody SignupRequest request) {
        SignUpResult result = authService.signUp(request.getUserId(), request.getPassword(), request.getNickname());
        SignUpResponse response = new SignUpResponse(result.getUserId(), result.getPassCode());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "회원ID 중복 확인", description = "전달된 회원ID를 기반으로 중복 여부를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값이 빈 값이거나 잘못된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(schema = @Schema(implementation = CheckResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값이 빈 값이거나 잘못된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResult result = authService.login(request.getUserId(), request.getPassword(), httpRequest.getRemoteAddr());
        LoginResponse response = new LoginResponse(result.getAccessToken(), result.getRefreshToken(), new UserResponse(result.getUser()));
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
        FindIdResponse response = new FindIdResponse(userId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "임시 비밀번호 발급", description = "회원ID와 패쓰코드를 입력받아 임시 비밀번호를 발급하는 API이다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공",
                    content = @Content(schema = @Schema(implementation = FindPasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 정보에 해당하는 사용자를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/find-password")
    public ResponseEntity<FindPasswordResponse> findPassword(
            @Parameter(name = "FindPasswordRequest", description = "임시 비밀번호 발급 요청 DTO (userId, passCode)", required = true)
            @RequestBody FindPasswordRequest request) {
        String tempPassword = authService.findPassword(request.getUserId(), request.getPassCode());
        FindPasswordResponse response = new FindPasswordResponse(tempPassword);
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
    @PostMapping("/token/refresh")
    public ResponseEntity<LoginResponse> updateToken(
            @Parameter(name = "RefreshTokenRequest", description = "토큰 갱신 요청 DTO (refreshToken)", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {
        String userId = jwtProvider.getUserIdFromToken(request.getRefreshToken());
        LoginResult result = authService.refreshTokenForMember(userId, request.getRefreshToken());
        LoginResponse response = new LoginResponse(result.getAccessToken(), result.getRefreshToken(), new UserResponse(result.getUser()));
        return ResponseEntity.ok().body(response);
    }
}
