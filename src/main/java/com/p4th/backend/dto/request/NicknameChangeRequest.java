package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "닉네임 변경 요청 DTO")
public class NicknameChangeRequest {
    @Schema(description = "새로운 닉네임", example = "새로운닉네임")
    private String nickname;
}
