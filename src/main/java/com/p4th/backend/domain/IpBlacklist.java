package com.p4th.backend.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpBlacklist {
    private String blacklistId;
    private String ipAddress;
    private String status; // "READY" 또는 "BLOCKED"
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
