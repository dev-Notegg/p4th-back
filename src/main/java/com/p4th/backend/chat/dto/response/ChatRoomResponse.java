package com.p4th.backend.chat.dto.response;

import com.p4th.backend.chat.domain.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {

    @Schema(description = "채팅방 ID")
    private String roomId;

    @Schema(description = "채팅방 유형 (DM / LOBBY)")
    private String roomType;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    public static ChatRoomResponse of(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomType(room.getRoomType().name())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
