package com.p4th.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "알림 응답 DTO")
public class NotificationResponse {
    @Schema(description = "알림 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String notificationId;

    @Schema(description = "사용자 ID", example = "id1234")
    private String userId;

    @Schema(description = "게시글 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0", nullable = true)
    private String postId;

    @Schema(description = "댓글 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0", nullable = true)
    private String commentId;

    @Schema(description = "알림 타입", example = "COMMENT")
    private String type;

    @Schema(description = "읽음 여부", example = "0")
    private int readYn;

    @Schema(description = "읽음 일시", example = "2025-02-04 12:10:00", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "생성일시", example = "2025-02-04 12:00:00")
    private LocalDateTime createdAt;
}
