package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private int recommend_yn;
    private String status;
    private LocalDateTime statusChangedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
