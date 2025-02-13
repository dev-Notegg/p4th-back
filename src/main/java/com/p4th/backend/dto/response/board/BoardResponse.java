package com.p4th.backend.dto.response.board;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardResponse {
    private String boardId;
    private String categoryId;
    private String boardName;
    private int boardLevel;
    private int sortOrder;
    private int recommendYn;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String categoryName;
}
