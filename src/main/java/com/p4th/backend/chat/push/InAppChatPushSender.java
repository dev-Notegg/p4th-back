package com.p4th.backend.chat.push;

import com.p4th.backend.chat.domain.ChatNotification;
import com.p4th.backend.chat.repository.ChatNotificationRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "chat.push.type", havingValue = "inapp", matchIfMissing = true)
public class InAppChatPushSender implements ChatPushSender {

    private final ChatNotificationRepository notificationRepository;

    @Override
    public void send(String receiverId, String roomId, String messageId, String previewContent) {
        ChatNotification notification = ChatNotification.builder()
                .notificationId(ULIDUtil.getULID())
                .receiverId(receiverId)
                .roomId(roomId)
                .messageId(messageId)
                .contentPreview(previewContent)
                .readYn(false)
                .build();

        notificationRepository.save(notification);
    }
}
