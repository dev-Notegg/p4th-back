package com.p4th.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "메시지 읽음 처리 요청 DTO")
public class ChatReadRequest {

    @Schema(description = "채팅방 ID", example = "dm_ABC123")
    private String roomId;

    @Schema(description = "사용자 ID", example = "userA")
    private String userId;

    @Schema(description = "마지막 읽은 메시지 ID", example = "msg_ABC123")
    private String messageId;
}
