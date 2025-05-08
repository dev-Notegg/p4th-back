package com.p4th.backend.chat.controller;

import com.p4th.backend.chat.dto.request.ChatMessageRequest;
import com.p4th.backend.chat.dto.request.ChatReadRequest;
import com.p4th.backend.chat.service.ChatSocketService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatSocketService chatSocketService;

    @MessageMapping("/chat.send.{roomId}")
    @Hidden
    public void sendMessage(ChatMessageRequest request) {
        log.debug("WebSocket SendMessage: {}", request);
        chatSocketService.handleSendMessage(request);
    }

    @MessageMapping("/chat.read.{roomId}")
    @Hidden
    public void readMessage(ChatReadRequest request) {
        log.debug("WebSocket ReadMessage: {}", request);
        chatSocketService.handleRead(request);
    }
}
