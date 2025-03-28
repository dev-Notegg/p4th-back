package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordChangeRequest {
    @Schema(description = "현재 비밀번호", example = "현재비밀번호")
    private String oldPassword;

    @Schema(description = "새로운 비밀번호", example = "새로운비밀번호")
    private String newPassword;
}
