package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "댓글 작성 요청 DTO")
public class CommentCreateRequest {
    @Schema(description = "댓글 내용", example = "댓글 내용 작성...")
    private String content;

    @Schema(description = "대댓글인 경우 부모 댓글 ID (없으면 null)", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String parentCommentId;
}
