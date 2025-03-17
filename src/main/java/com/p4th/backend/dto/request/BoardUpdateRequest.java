package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "게시판 수정 요청 DTO")
public class BoardUpdateRequest {
    @Schema(description = "게시판명", example = "수정된 게시판명")
    private String boardName;
    @Schema(description = "카테고리 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String categoryId;
    @Schema(description = "게시판 레벨", example = "2")
    private int boardLevel;
}
