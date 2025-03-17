package com.p4th.backend.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.p4th.backend.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "게시판 응답 DTO")
public class BoardResponse {
    @Schema(description = "게시판 ID")
    private String boardId;
    @Schema(description = "게시판명")
    private String boardName;
    @Schema(description = "카테고리 ID")
    private String categoryId;
    @Schema(description = "카테고리명")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryName;
    @Schema(description = "게시판 레벨")
    private int boardLevel;
    @Schema(description = "추천 여부")
    private int recommendYn;
    @Schema(description = "정렬 순서")
    private int sortOrder;
    @Schema(description = "최근 수정/생성일", example = "2025-01-01")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String recentlyModified;

    public BoardResponse(String boardId, String boardName, String categoryId, String categoryName,
                         int boardLevel, int recommendYn, int sortOrder,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.boardLevel = boardLevel;
        this.recommendYn = recommendYn;
        this.sortOrder = sortOrder;
        // 최근 수정일이 있으면 수정일, 없으면 생성일을 'yyyy-MM-dd' 형식으로 설정
        LocalDateTime dateToShow = updatedAt != null ? updatedAt : createdAt;
        if(dateToShow != null) {
            this.recentlyModified = dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    public BoardResponse() {
    }

    public static BoardResponse from(Board board) {
        LocalDateTime dateToShow = board.getUpdatedAt() != null ? board.getUpdatedAt() : board.getCreatedAt();
        String recentlyModified = (dateToShow != null) ? dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
        BoardResponse dto = new BoardResponse();
        dto.setBoardId(board.getBoardId());
        dto.setBoardName(board.getBoardName());
        dto.setCategoryId(board.getCategoryId());
        dto.setCategoryName(board.getCategory() != null ? board.getCategory().getCategoryName() : null);
        dto.setBoardLevel(board.getBoardLevel());
        dto.setRecommendYn(board.getRecommendYn());
        dto.setSortOrder(board.getSortOrder());
        dto.setRecentlyModified(recentlyModified);
        return dto;
    }
}