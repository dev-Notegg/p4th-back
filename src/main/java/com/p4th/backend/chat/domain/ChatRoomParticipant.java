package com.p4th.backend.chat.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_participant")
@IdClass(ChatRoomParticipantId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipant {

    @Id
    private String roomId;

    @Id
    private String userId;

    @Column(nullable = false)
    private String nickname;

    private LocalDateTime joinedAt;

    private String lastReadMessageId;

    @PrePersist
    public void prePersist() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}
