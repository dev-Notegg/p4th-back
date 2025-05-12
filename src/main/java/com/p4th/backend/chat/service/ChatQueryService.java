package com.p4th.backend.chat.service;

import com.p4th.backend.chat.domain.ChatMessage;
import com.p4th.backend.chat.domain.ChatRoom;
import com.p4th.backend.chat.dto.response.ChatMessageResponse;
import com.p4th.backend.chat.dto.response.ChatRoomListResponse;
import com.p4th.backend.chat.repository.ChatMessageRepository;
import com.p4th.backend.chat.repository.ChatRoomParticipantRepository;
import com.p4th.backend.chat.repository.ChatRoomRepository;
import com.p4th.backend.chat.util.ChatTimeUtil;
import com.p4th.backend.chat.websocket.ChatPresenceTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPresenceTracker chatPresenceTracker;

    public Page<ChatMessageResponse> getMessages(String roomId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        return ChatMessageResponse.fromPage(messagePage);
    }

    public List<ChatRoomListResponse> getDmRooms(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllDmRoomsByUser(userId);

        return rooms.stream().map(room -> {
            String roomId = room.getRoomId();
            String opponentId = chatRoomParticipantRepository.findOpponentId(roomId, userId);
            String nickname = chatRoomParticipantRepository.findNickname(roomId, opponentId);
            boolean isOnline = chatPresenceTracker.isOnline(roomId, opponentId);

            // 마지막 메시지
            List<ChatMessage> lastMessages = chatMessageRepository.findTop1ByRoomIdOrderByCreatedAtDesc(roomId);
            ChatMessage lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);
            String lastMessageContent = "";

            if (lastMessage != null) {
                lastMessageContent = lastMessage.getMessageType() == ChatMessage.MessageType.IMAGE
                        ? "[이미지]"
                        : ChatTimeUtil.getPreview(lastMessage.getContent());
            }

            String lastMessageTime = (lastMessage != null)
                    ? ChatTimeUtil.getRelativeTime(lastMessage.getCreatedAt())
                    : "";

            return ChatRoomListResponse.builder()
                    .roomId(roomId)
                    .opponentId(opponentId)
                    .opponentNickname(nickname)
                    .online(isOnline)
                    .lastMessageContent(lastMessageContent)
                    .lastMessageTime(lastMessageTime)
                    .build();
        }).toList();
    }
}
