package com.p4th.backend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    public enum MessageType {
        TEXT, IMAGE
    }

    @Id
    @Column(name = "message_id", length = 26)
    private String messageId;

    @Column(name = "room_id", length = 26, nullable = false)
    private String roomId;

    @Column(name = "sender_id", length = 26, nullable = false)
    private String senderId;

    @Column(name = "sender_nickname", length = 100, nullable = false)
    private String senderNickname;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
