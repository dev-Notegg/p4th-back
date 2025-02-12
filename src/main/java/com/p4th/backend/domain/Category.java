package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {
    @Id
    private String categoryId;
    private String categoryName;
    private int sortOrder;
    private int mainExposure;
    private int isNotice;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Board> boards;
}
