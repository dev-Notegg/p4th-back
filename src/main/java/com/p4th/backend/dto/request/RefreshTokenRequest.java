package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    @Schema(description = "발급받은 리프레시 토큰")
    @NotBlank
    private String refreshToken;
}
