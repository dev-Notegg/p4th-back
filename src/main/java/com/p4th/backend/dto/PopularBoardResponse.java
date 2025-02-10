package com.p4th.backend.dto;

import lombok.Data;

@Data
public class PopularBoardResponse {
    private String boardId;
    private String category;
    private String boardName;
}
