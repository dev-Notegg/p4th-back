package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "알림 읽음 처리 요청 DTO")
public class NotificationReadRequest {
    @Schema(description = "알림 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String notificationId;

    @Schema(description = "읽음 여부", example = "1")
    private int readYn;

    @Schema(description = "읽음 일시", example = "2025-02-04 12:30:00")
    private LocalDateTime readAt;
}
