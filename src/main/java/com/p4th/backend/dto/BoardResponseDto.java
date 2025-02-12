package com.p4th.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoardResponseDto {
    private String boardId;
    private String categoryId;
    private String boardName;
    private int boardLevel;
    private int sortOrder;
    private int recommend_yn;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String categoryName;
}
