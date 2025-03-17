package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "게시판 생성 응답 DTO")
public class BoardCreationResponse {
    @Schema(description = "생성된 게시판 ID")
    private String boardId;

    public BoardCreationResponse(String boardId) {
        this.boardId = boardId;
    }
}
