package com.p4th.backend.dto.response.post;

import com.p4th.backend.domain.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "인기 게시글 조회 응답 DTO")
public class PopularPostResponse {
    @Schema(description = "게시글 ID", example = "01HXYA5X2V6N3QZQF9LZBHY7TW")
    private String postId;

    @Schema(description = "게시판 ID", example = "01HXYA4V8D5N1PZQF8KZBHY6XT")
    private String boardId;

    @Schema(description = "카테고리명", example = "일반 카테고리")
    private String categoryName;

    @Schema(description = "게시판명", example = "자유게시판")
    private String boardName;

    @Schema(description = "작성자 ID", example = "user_001")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "닉네임")
    private String nickname;

    @Schema(description = "게시글 제목", example = "첫 번째 게시글")
    private String title;

    @Schema(description = "게시글 내용", example = "가나다라마바사")
    private String content;

    @Schema(description = "게시글 상태", example = "NORMAL")
    private PostStatus status;

    @Schema(description = "조회수", example = "19")
    private int viewCount;

    @Schema(description = "댓글 수", example = "2")
    private int commentCount;

    @Schema(description = "썸네일 이미지 URL", example = "https://picsum.photos/500")
    private String imageUrl;

    @Schema(description = "이미지 개수", example = "1")
    private int imageCount;

    @Schema(description = "게시글 작성일 (상대 시간 형식)", example = "0분 전")
    private String createdAt;
}