package com.p4th.backend.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresenceEvent {
    private String roomId;
    private String userId;
    private boolean online;
}
