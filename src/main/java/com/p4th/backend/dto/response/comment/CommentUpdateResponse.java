package com.p4th.backend.dto.response.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "댓글 수정 응답 DTO")
public class CommentUpdateResponse {
    @Schema(description = "수정 성공 여부", example = "true")
    private boolean updated;

    public CommentUpdateResponse(boolean updated) {
        this.updated = updated;
    }
}
