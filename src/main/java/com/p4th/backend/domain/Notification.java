package com.p4th.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {
    @Id
    private String notificationId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "post_id")
    private String postId;
    private String commentId;
    private NotificationType type;      // COMMENT, RECOMMENT, NOTICE, ALERT 등
    private String title;
    private String content;
    private int readYn;       // 0: 읽지 않음, 1: 읽음
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private String createdBy;
}
