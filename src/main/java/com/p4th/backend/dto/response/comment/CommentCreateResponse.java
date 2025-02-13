package com.p4th.backend.dto.response.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "댓글 작성 응답 DTO")
public class CommentCreateResponse {
    @Schema(description = "생성된 댓글 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String commentId;

    public CommentCreateResponse(String commentId) {
        this.commentId = commentId;
    }
}
