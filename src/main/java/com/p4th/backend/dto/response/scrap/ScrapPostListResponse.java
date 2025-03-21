package com.p4th.backend.dto.response.scrap;

import com.p4th.backend.domain.Post;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.util.HtmlContentUtils;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "스크랩 게시글 목록 응답 DTO (스크랩 ID 포함)")
@EqualsAndHashCode(callSuper = false)
public class ScrapPostListResponse extends PostListResponse {
    @Schema(description = "스크랩 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapId;
    @Schema(description = "스크랩 폴더 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapFolderId;

    public static ScrapPostListResponse from(Post post, String scrapId, String scrapFolderId) {
        ScrapPostListResponse response = new ScrapPostListResponse();
        response.setPostId(post.getPostId());
        response.setBoardId(post.getBoardId());
        response.setUserId(post.getUserId());
        response.setNickname(post.getUser() != null ? post.getUser().getNickname() : "");
        response.setTitle(HtmlContentUtils.extractText(post.getTitle(), 30));
        if (post.getBoard() != null) {
            response.setBoardName(post.getBoard().getBoardName());
            if (post.getBoard().getCategory() != null) {
                response.setCategoryName(post.getBoard().getCategory().getCategoryName());
            } else {
                response.setCategoryName(post.getBoard().getCategoryName());
            }
        }
        response.setStatus(post.getStatus());
        response.setViewCount(post.getViewCount());
        response.setCommentCount(post.getCommentCount());
        response.setImageUrl(HtmlImageUtils.extractFirstImageUrl(post.getContent()));
        response.setImageCount(HtmlImageUtils.countInlineImages(post.getContent()));
        response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()));
        response.setScrapFolderId(scrapFolderId);
        response.setScrapId(scrapId);
        return response;
    }
}
