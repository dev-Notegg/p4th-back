package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {
    private String notificationId;
    private String userId;
    private String postId;
    private String commentId;
    private NotificationType type;      // COMMENT, RECOMMENT, NOTICE, ALERT 등
    private int readYn;       // 0: 읽지 않음, 1: 읽음
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private String createdBy;
}
