package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScrapFolder {
    private String scrapFolderId;
    private String userId;
    private String folderName;
    private int sortOrder;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
