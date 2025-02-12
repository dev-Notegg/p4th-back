package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "게시글 등록 요청 DTO")
public class RegisterPostRequest {
    @Schema(description = "게시판 ID", example = "01HXYA4V8D5N1PZQF8KZBHY7XT")
    private String boardId;

    @Schema(description = "게시글 제목", example = "게시글 제목")
    private String title;

    @Schema(description = "게시글 내용 (HTML 형식)", example = "<iframe class=\"ql-video\" ...></iframe><img src=\"https://picsum.photos/500\" ... /><p>내용</p>")
    private String content;
}
