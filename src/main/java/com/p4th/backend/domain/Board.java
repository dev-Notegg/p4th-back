package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board")
public class Board {
    @Id
    private String boardId;
    @Column(name = "category_id")
    private String categoryId;
    private String boardName;
    private int boardLevel;
    private int sortOrder;
    private String createdBy;
    private String updatedBy;
    // 연관관계 필드 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}
