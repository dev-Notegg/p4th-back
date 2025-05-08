package com.p4th.backend.chat.push;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(ChatPushSender.class)
public class NoopChatPushSender implements ChatPushSender {
    @Override
    public void send(String receiverId, String roomId, String messageId, String previewContent) {
        // no-op
    }
}
