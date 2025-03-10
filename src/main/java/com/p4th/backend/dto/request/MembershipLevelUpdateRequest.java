package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MembershipLevelUpdateRequest {
    @Schema(description = "회원 등급", example = "1")
    private int membershipLevel;
}
