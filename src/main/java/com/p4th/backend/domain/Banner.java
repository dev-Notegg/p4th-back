package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Banner {
    private String bannerId;
    private String bannerName;
    private String imageUrl;
    private String linkUrl;
    private int displayYn;
    private int seq;
    private String startDate;
    private String endDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
