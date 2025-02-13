package com.p4th.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String createdBy;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private String updatedBy;
    @JsonIgnore
    private LocalDateTime updatedAt;
}
