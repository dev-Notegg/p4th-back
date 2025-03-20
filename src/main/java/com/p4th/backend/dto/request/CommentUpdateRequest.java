package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "댓글 수정 요청 DTO")
public class CommentUpdateRequest {
    @Schema(description = "수정된 댓글 내용", example = "수정된 댓글 내용")
    private String content;
    @Schema(description = "비밀 댓글 여부", example = "true")
    private Boolean secretYn;
}
