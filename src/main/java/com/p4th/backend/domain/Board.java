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
    private String categoryId;
    private String boardName;
    private int boardLevel;
    private int sortOrder;
    private String createdBy;
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}
