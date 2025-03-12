package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostHistoryLog {
    private String historyId;
    private String postId;
    private String periodType; // DAILY, WEEKLY, MONTHLY
    private LocalDateTime periodStartDate;
    private int viewCount;
    private int commentCount;
    private float popularityScore;
    private String createdBy;
    private LocalDateTime createdAt;
}
