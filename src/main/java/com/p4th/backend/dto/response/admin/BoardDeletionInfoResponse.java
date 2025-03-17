package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "게시판 삭제 전 정보 조회 응답 DTO")
public class BoardDeletionInfoResponse {
    @Schema(description = "게시판명")
    private String boardName;
    @Schema(description = "게시글 수")
    private int postCount;
    @Schema(description = "댓글 수")
    private int commentCount;

    public BoardDeletionInfoResponse(String boardName, int postCount, int commentCount) {
        this.boardName = boardName;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }
}
