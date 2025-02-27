package com.p4th.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String createdBy;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private String updatedBy;
    @JsonIgnore
    private LocalDateTime updatedAt;
    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Board> boards;

    public boolean isNotice() {
        return this.isNotice == 1;
    }
}
