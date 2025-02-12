package com.p4th.backend.dto.response;

import com.p4th.backend.domain.Post;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.format.DateTimeFormatter;

@Data
public class PostListDto {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String category;
    private String boardName;
    private int viewCount;
    private int commentCount;
    private String imageUrl;
    private int imageCount;
    private String createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostListDto from(Post post) {
        PostListDto dto = new PostListDto();
        dto.postId = post.getPostId();
        dto.boardId = post.getBoardId();
        dto.userId = post.getUserId();
        dto.nickname = post.getUser() != null ? post.getUser().getNickname() : "";
        dto.title = post.getTitle();
        if (post.getBoard() != null) {
            dto.boardName = post.getBoard().getBoardName();
            if (post.getBoard().getCategory() != null) {
                dto.category = post.getBoard().getCategory().getCategoryName();
            }
        }
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();

        // 첨부파일 개수
        int attachmentCount = (post.getAttachments() != null) ? post.getAttachments().size() : 0;
        // HTML 컨텐츠 내의 <img> 태그(이미지 확장자인 경우)의 개수
        int inlineImageCount = countInlineImages(post.getContent());
        dto.imageCount = attachmentCount + inlineImageCount;

        // 우선순위: HTML 내의 첫 번째 <img> 태그의 src 사용, 없으면 첨부파일 첫 번째 URL 사용
        String inlineImageUrl = extractFirstImageUrl(post.getContent());
        if (inlineImageUrl != null) {
            dto.imageUrl = inlineImageUrl;
        } else if (attachmentCount > 0) {
            dto.imageUrl = post.getAttachments().get(0).getFileUrl();
        } else {
            dto.imageUrl = null;
        }
        if (post.getCreatedAt() != null) {
            dto.createdAt = post.getCreatedAt().format(formatter);
        }
        return dto;
    }

    /**
     * HTML 문자열에서 첫 번째 <img> 태그의 src 속성을 추출한다.
     * 단, 추출된 URL이 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)인 경우에만 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 첫 번째 이미지 URL (이미지 확장자인 경우), 아니면 null
     */
    private static String extractFirstImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        Document doc = Jsoup.parse(htmlContent);
        Element img = doc.selectFirst("img[src]");
        if (img != null) {
            String src = img.attr("src");
            // 이미지 확장자 검사 (대소문자 무시)
            if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
                return src;
            }
        }
        return null;
    }

    /**
     * HTML 문자열 내의 <img> 태그 중, src 속성이 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)인 태그의 개수를 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 이미지 태그 개수
     */
    private static int countInlineImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return 0;
        }
        Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img[src]");
        int count = 0;
        for (Element img : imgs) {
            String src = img.attr("src");
            if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
                count++;
            }
        }
        return count;
    }
}
