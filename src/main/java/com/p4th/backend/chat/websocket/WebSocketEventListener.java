package com.p4th.backend.chat.websocket;

import com.p4th.backend.chat.dto.response.PresenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatPresenceTracker chatPresenceTracker;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getFirstNativeHeader("X-USER-ID");
        String roomId = accessor.getFirstNativeHeader("X-ROOM-ID");

        if (userId != null && roomId != null) {
            chatPresenceTracker.addUser(roomId, userId);
            messagingTemplate.convertAndSend("/topic/presence." + roomId,
                    new PresenceEvent(roomId, userId, true));
            log.info("CONNECT: user {} joined room {}", userId, roomId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getFirstNativeHeader("X-USER-ID");
        String roomId = accessor.getFirstNativeHeader("X-ROOM-ID");

        if (userId != null && roomId != null) {
            chatPresenceTracker.removeUser(roomId, userId);
            messagingTemplate.convertAndSend("/topic/presence." + roomId,
                    new PresenceEvent(roomId, userId, false));
            log.info("DISCONNECT: user {} left room {}", userId, roomId);
        }
    }
}
