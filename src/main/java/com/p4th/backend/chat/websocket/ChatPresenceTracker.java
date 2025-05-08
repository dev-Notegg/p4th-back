package com.p4th.backend.chat.websocket;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatPresenceTracker {

    // roomId -> userId Set
    private final Map<String, Set<String>> onlineUsersByRoom = new ConcurrentHashMap<>();

    public void addUser(String roomId, String userId) {
        onlineUsersByRoom.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public boolean isOnline(String roomId, String userId) {
        return onlineUsersByRoom.getOrDefault(roomId, Collections.emptySet()).contains(userId);
    }
}
