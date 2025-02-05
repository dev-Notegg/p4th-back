package com.p4th.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {
    @Schema(description = "로그인 ID", example = "id1234")
    private String loginId;

    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @Schema(description = "닉네임", example = "John")
    private String nickname;
}