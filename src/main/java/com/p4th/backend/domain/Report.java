package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
public class Report {
    @Id
    private String reportId;
    private String reporterId;
    private String targetUserId;
    private String targetId;
    @Enumerated(EnumType.STRING)
    private ReportType type; // POST, COMMENT
    private int readYn; // 0: 미확인, 1: 확인
    private LocalDateTime readAt;
    private String reason;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
