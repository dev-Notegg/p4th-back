package com.p4th.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "banner")
public class Banner {
    @Id
    private String bannerId;
    private String bannerName;
    private String imageUrl;
    private String linkUrl;
    private Integer seq;
    private LocalDate startDate;  // 광고 시작일
    private LocalDate endDate;    // 광고 종료일
    @JsonIgnore
    private String createdBy;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private String updatedBy;
    @JsonIgnore
    private LocalDateTime updatedAt;
}
