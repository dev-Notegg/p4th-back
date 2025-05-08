package com.p4th.backend.chat.service;

import com.p4th.backend.chat.domain.ChatMessage;
import com.p4th.backend.chat.domain.ChatRoom;
import com.p4th.backend.chat.domain.ChatRoomParticipant;
import com.p4th.backend.chat.dto.request.ChatMessageRequest;
import com.p4th.backend.chat.dto.response.ChatMessageResponse;
import com.p4th.backend.chat.repository.ChatMessageRepository;
import com.p4th.backend.chat.repository.ChatRoomParticipantRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRoomCommandService chatRoomCommandService;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        String roomId = request.getRoomId();

        // DM 채팅방 자동 생성
        if (roomId == null || roomId.isBlank()) {
            if (request.getReceiverId() == null || request.getReceiverId().isBlank()) {
                throw new IllegalArgumentException("메세지 받는 사람 ID가 필요합니다.");
            }
            if (request.getReceiverNickname() == null || request.getReceiverNickname().isBlank()) {
                throw new IllegalArgumentException("메세지 받는 사람 닉네임이 필요합니다.");
            }

            ChatRoom dmRoom = chatRoomCommandService.getOrCreateDmRoom(
                    request.getSenderId(),
                    request.getSenderNickname(),
                    request.getReceiverId(),
                    request.getReceiverNickname()
            );
            roomId = dmRoom.getRoomId();
            request.setRoomId(roomId);
        }

        // 메시지 저장
        String messageId = ULIDUtil.getULID();
        ChatMessage message = ChatMessage.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(request.getSenderId())
                .senderNickname(request.getSenderNickname())
                .content(request.getContent())
                .messageType(ChatMessage.MessageType.valueOf(request.getMessageType()))
                .build();

        chatMessageRepository.save(message);

        // 읽음 처리 (로비는 제외)
        if (!"lobby".equals(roomId)) {
            ChatRoomParticipant participant = chatRoomParticipantRepository
                    .findByRoomIdAndUserId(roomId, request.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("채팅방 참여자 아님"));
            participant.setLastReadMessageId(messageId);
            chatRoomParticipantRepository.save(participant);
        }

        return ChatMessageResponse.from(message);
    }
}
