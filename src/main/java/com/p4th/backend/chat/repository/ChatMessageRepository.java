package com.p4th.backend.chat.repository;

import com.p4th.backend.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    Page<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId, Pageable pageable);

    List<ChatMessage> findTop1ByRoomIdOrderByCreatedAtDesc(String roomId);
}
