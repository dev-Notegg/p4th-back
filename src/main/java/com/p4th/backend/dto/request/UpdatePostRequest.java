package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "게시글 수정 요청 DTO")
public class UpdatePostRequest {
    @Schema(description = "게시글 제목", example = "수정된 게시글 제목")
    private String title;

    @Schema(description = "게시글 내용 (HTML 형식)", example = "<iframe class=\"ql-video\" ...></iframe><img src=\"https://picsum.photos/500\" ... /><p>수정된 내용</p>")
    private String content;
}
