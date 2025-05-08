package com.p4th.backend.chat.service;

import com.p4th.backend.chat.dto.request.ChatMessageRequest;
import com.p4th.backend.chat.dto.request.ChatReadRequest;
import com.p4th.backend.chat.dto.response.ChatMessageResponse;
import com.p4th.backend.chat.dto.response.ChatNotificationPayload;
import com.p4th.backend.chat.domain.ChatRoomParticipant;
import com.p4th.backend.chat.push.ChatPushSender;
import com.p4th.backend.chat.repository.ChatRoomParticipantRepository;
import com.p4th.backend.chat.util.ChatTimeUtil;
import com.p4th.backend.chat.websocket.ChatPresenceTracker;
import com.p4th.backend.common.exception.GlobalExceptionHandler;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSocketService {

    private final ChatService chatService;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatPushSender chatPushSender;
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ChatPresenceTracker chatPresenceTracker;

    @Transactional
    public void handleSendMessage(ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(request);

        // 채팅 메시지 전송
        try {
            messagingTemplate.convertAndSend("/topic/chat." + response.getRoomId(), response);
        } catch (Exception e) {
            logger.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
        }

        // 로비는 참여자 저장이 없으므로 알림/읽음 처리 없음
        if ("lobby".equals(response.getRoomId())) {
            return;
        }

        // DM - 상대방에게 알림 전송
        List<String> participantIds = chatRoomParticipantRepository.findByRoomId(response.getRoomId())
                .stream()
                .map(ChatRoomParticipant::getUserId)
                .filter(id -> !id.equals(response.getSenderId()))
                .toList();

        for (String receiverId : participantIds) {
            String preview = "IMAGE".equalsIgnoreCase(request.getMessageType())
                    ? "[이미지]"
                    : ChatTimeUtil.getPreview(request.getContent());

            ChatNotificationPayload payload = ChatNotificationPayload.builder()
                    .notificationId(ULIDUtil.getULID())
                    .roomId(response.getRoomId())
                    .messageId(response.getMessageId())
                    .contentPreview(preview)
                    .build();

            messagingTemplate.convertAndSend("/topic/notify." + receiverId, payload);
            chatPushSender.send(receiverId, response.getRoomId(), response.getMessageId(), preview);
        }

        chatPresenceTracker.addUser(request.getRoomId(), request.getSenderId());
    }

    @Transactional
    public void handleRead(ChatReadRequest request) {
        if ("lobby".equals(request.getRoomId())) {
            return; // 로비는 참여자 관리 안함
        }
        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByRoomIdAndUserId(request.getRoomId(), request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여자 정보를 찾을 수 없습니다."));

        participant.setLastReadMessageId(request.getMessageId());
        chatRoomParticipantRepository.save(participant);

        try {
            messagingTemplate.convertAndSend("/topic/read." + request.getRoomId(), request);
        } catch (Exception e) {
            logger.warn("읽음 메시지 브로드캐스트 실패: {}", e.getMessage());
        }
    }
}
