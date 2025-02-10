package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostHistoryLog {
    private String historyId;
    private String postId;
    private String periodType; // DAILY, WEEKLY, MONTHLY
    private String periodStartDate;
    private String periodEndDate;
    private int viewCount;
    private int commentCount;
    private float popularityScore;
    private String createdBy;
}
