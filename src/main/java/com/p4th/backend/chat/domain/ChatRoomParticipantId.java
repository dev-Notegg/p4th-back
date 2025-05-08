package com.p4th.backend.chat.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipantId implements Serializable {
    private String roomId;
    private String userId;
}
