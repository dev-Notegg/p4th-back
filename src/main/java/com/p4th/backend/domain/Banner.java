package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

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
}
