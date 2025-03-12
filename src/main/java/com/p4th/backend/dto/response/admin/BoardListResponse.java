package com.p4th.backend.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "게시판 목록 응답 DTO")
public class BoardListResponse {
    @Schema(description = "게시판 목록")
    private List<BoardResponse> boards;

    public BoardListResponse(List<BoardResponse> boards) {
        this.boards = boards;
    }
}
