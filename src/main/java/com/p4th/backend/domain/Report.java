package com.p4th.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String targetBoardId;
    private String targetCommentId;
    private ReportType type;         // POST 또는 COMMENT
    private ReportStatus status;     // WAIT, DONE, REJECT, CANCEL
    private String reason;
    private LocalDateTime processedAt;
    private String processedBy;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
