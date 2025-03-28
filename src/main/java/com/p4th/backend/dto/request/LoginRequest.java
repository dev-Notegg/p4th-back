package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @Schema(description = "회원 ID")
    private String userId;
    @Schema(description = "비밀번호")
    private String password;
}
