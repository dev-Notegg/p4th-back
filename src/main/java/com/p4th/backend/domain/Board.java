package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Board {
    private String boardId;
    private String categoryId;
    private String boardName;
    private int boardLevel;
    private int sortOrder;
    private String createdBy;
    private String updatedBy;
}
