package com.p4th.backend.chat.dto.response;

import com.p4th.backend.chat.domain.ChatMessage;
import com.p4th.backend.chat.util.ChatTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Getter
@Builder
public class ChatMessageResponse {

    @Schema(description = "메시지 ID")
    private final String messageId;

    @Schema(description = "채팅방 ID")
    private final String roomId;

    @Schema(description = "보낸 사람 ID")
    private final String senderId;

    @Schema(description = "보낸 사람 닉네임")
    private final String senderNickname;

    @Schema(description = "메시지 본문")
    private final String content;

    @Schema(description = "메시지 타입(TEXT/IMAGE)")
    private final String messageType;

    @Schema(description = "메시지 생성 시각")
    private final LocalDateTime createdAt;

    @Schema(description = "상대 시간 표현 (예: 방금 전, 5분 전)")
    private final String relativeTime;

    public static ChatMessageResponse from(ChatMessage entity) {
        return ChatMessageResponse.builder()
                .messageId(entity.getMessageId())
                .roomId(entity.getRoomId())
                .senderId(entity.getSenderId())
                .senderNickname(entity.getSenderNickname())
                .content(entity.getContent())
                .messageType(entity.getMessageType().name())
                .createdAt(entity.getCreatedAt())
                .relativeTime(ChatTimeUtil.getRelativeTime(entity.getCreatedAt()))
                .build();
    }

    public static Page<ChatMessageResponse> fromPage(Page<ChatMessage> page) {
        List<ChatMessageResponse> content = page.getContent().stream()
                .map(ChatMessageResponse::from)
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }
}
