package com.p4th.backend.chat.repository;

import com.p4th.backend.chat.domain.ChatNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatNotificationRepository extends JpaRepository<ChatNotification, String> {
}
