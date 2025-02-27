package com.p4th.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "읽지 않은 알림 개수 응답 DTO")
public class UnreadCountResponse {
    @Schema(description = "읽지 않은 알림 개수", example = "5")
    private int unreadCount;
}
