package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Category {
    private String categoryId;
    private String categoryName;
    private int sortOrder;
    private int mainExposure;
    private int isNotice;
    private List<Board> boards;
}
