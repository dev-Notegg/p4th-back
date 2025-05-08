package com.p4th.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListResponse {

    @Schema(description = "채팅방 ID")
    private String roomId;

    @Schema(description = "상대방 ID")
    private String opponentId;

    @Schema(description = "상대방 닉네임")
    private String opponentNickname;

    @Schema(description = "상대방 접속 여부")
    private boolean online;

    @Schema(description = "최근 메시지 내용")
    private String lastMessageContent;

    @Schema(description = "최근 메시지 상대 시간", example = "3분 전")
    private String lastMessageTime;
}
