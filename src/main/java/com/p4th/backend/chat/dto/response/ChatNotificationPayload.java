package com.p4th.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "채팅 알림 WebSocket 전송용 DTO")
public class ChatNotificationPayload {

    @Schema(description = "알림 ID")
    private String notificationId;

    @Schema(description = "채팅방 ID")
    private String roomId;

    @Schema(description = "메시지 ID")
    private String messageId;

    @Schema(description = "알림 본문 미리보기")
    private String contentPreview;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
}
