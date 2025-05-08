package com.p4th.backend.chat.push;

public interface ChatPushSender {
    void send(String receiverId, String roomId, String messageId, String previewContent);
}
