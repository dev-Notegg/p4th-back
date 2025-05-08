package com.p4th.backend.chat.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "chat.push.type", havingValue = "fcm", matchIfMissing = true)
public class FcmChatPushSender implements ChatPushSender {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/test/messages:send";
    private static final String SERVER_KEY = "BEARER";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String receiverId, String roomId, String messageId, String previewContent) {
        log.info("FCM 전송 준비 → receiver: {}, room: {}, msg: {}, preview: {}", receiverId, roomId, messageId, previewContent);

        String fcmToken = getFcmTokenByUserId(receiverId); // TODO: 사용자별 토큰 조회 필요

        if (fcmToken == null) {
            log.warn("FCM 토큰 없음 → receiverId: {}", receiverId);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(SERVER_KEY); // 또는 set("Authorization", "Bearer ...")

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "새 메시지");
        notification.put("body", previewContent);

        Map<String, Object> message = new HashMap<>();
        message.put("token", fcmToken);
        message.put("notification", notification);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(FCM_API_URL, request, String.class);
            log.info("FCM 응답: {}", response.getBody());
        } catch (Exception e) {
            log.error("FCM 전송 실패: {}", e.getMessage());
        }
    }

    private String getFcmTokenByUserId(String userId) {
        // TODO: 사용자 ID에 따른 FCM 디바이스 토큰을 조회하는 로직 구현 필요
        return null;
    }
}