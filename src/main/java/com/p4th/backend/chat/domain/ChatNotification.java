package com.p4th.backend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatNotification {

    @Id
    @Column(name = "notification_id", length = 26)
    private String notificationId;

    @Column(name = "receiver_id", length = 26, nullable = false)
    private String receiverId;

    @Column(name = "room_id", length = 26, nullable = false)
    private String roomId;

    @Column(name = "message_id", length = 26)
    private String messageId;

    @Column(name = "content_preview", length = 100)
    private String contentPreview;

    @Builder.Default
    @Column(name = "read_yn", nullable = false)
    private boolean readYn = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
