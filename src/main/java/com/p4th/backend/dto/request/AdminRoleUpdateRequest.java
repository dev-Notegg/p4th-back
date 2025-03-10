package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdminRoleUpdateRequest {
    @Schema(description = "관리자 권한", example = "1")
    private int adminRole;
}
