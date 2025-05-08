package com.p4th.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class ChatMessageRequest {

    @Schema(description = "채팅방 ID (로비: 'lobby', DM은 없으면 자동 생성)", example = "lobby")
    private String roomId;

    @Schema(description = "보내는 사람 ID", example = "userA")
    private String senderId;

    @Schema(description = "보내는 사람 닉네임", example = "체리버터")
    private String senderNickname;

    @Schema(description = "메시지 본문 (최대 1000자)", example = "안녕!")
    @NotBlank
    @Size(max = 1000, message = "메시지는 최대 1000자까지 입력 가능합니다.")
    private String content;

    @Schema(description = "메시지 타입 (TEXT 또는 IMAGE)", example = "TEXT")
    private String messageType;

    @Schema(description = "수신자 ID (DM 생성용, 로비는 필요 없음)", example = "userB")
    private String receiverId;

    @Schema(description = "수신자 닉네임 (DM 자동 생성 시 필요)", example = "상대방 닉네임")
    private String receiverNickname;
}
