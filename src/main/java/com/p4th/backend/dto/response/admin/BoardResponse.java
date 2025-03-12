package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "게시판 응답 DTO")
public class BoardResponse {
    @Schema(description = "게시판 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String boardId;

    @Schema(description = "게시판명", example = "공지사항")
    private String boardName;

    @Schema(description = "정렬 순서", example = "0")
    private int sortOrder;

    @Schema(description = "게시판 레벨", example = "0")
    private int boardLevel;

    public static BoardResponse from(com.p4th.backend.domain.Board board) {
        BoardResponse dto = new BoardResponse();
        dto.setBoardId(board.getBoardId());
        dto.setBoardName(board.getBoardName());
        dto.setSortOrder(board.getSortOrder());
        dto.setBoardLevel(board.getBoardLevel());
        return dto;
    }
}
