package com.p4th.backend.dto.response.post;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.format.DateTimeFormatter;
import org.apache.commons.text.StringEscapeUtils;

@Data
public class PostListResponse {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String category;    // 게시판의 카테고리명
    private String boardName;   // 게시판명
    private PostStatus status;
    private int viewCount;
    private int commentCount;
    private String imageUrl;    // 썸네일 이미지 URL
    private int imageCount;     // HTML 내 이미지 태그 개수
    private String createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostListResponse from(Post post) {
        PostListResponse dto = new PostListResponse();
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
        dto.status = post.getStatus();
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();

        // content가 DB에 이스케이프되어 저장된 경우 unescape 처리
        String unescapedContent = StringEscapeUtils.unescapeHtml4(post.getContent());

        // HTML 내 이미지 태그 처리
        if (!PostStatus.DELETED.equals(post.getStatus())) {
            dto.imageCount = countInlineImages(unescapedContent);
            dto.imageUrl = extractFirstImageUrl(unescapedContent);
        }
        if (post.getCreatedAt() != null) {
            dto.createdAt = RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt());
        }
        return dto;
    }

    /**
     * HTML 문자열에서 첫 번째 <img> 태그의 src를 추출한다.
     * 단, src가 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)인 경우에만 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 첫 번째 이미지 URL (이미지인 경우), 없으면 null
     */
    public static String extractFirstImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        Document doc = Jsoup.parse(htmlContent);
        // 여러 <img> 태그가 있을 경우 조건에 맞는 첫 번째 태그의 src 반환
        Elements imgs = doc.select("img[src]");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (isImageUrl(src)) {
                return src;
            }
        }
        return null;
    }

    /**
     * HTML 문자열 내의 <img> 태그 중, src가 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)인 태그의 개수를 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 이미지 태그의 개수
     */
    public static int countInlineImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return 0;
        }
        Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img[src]");
        int count = 0;
        for (Element img : imgs) {
            String src = img.attr("src");
            if (isImageUrl(src)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 주어진 src URL이 이미지로 간주될 수 있는지 판단한다.
     * 조건:
     * 1. src가 jpg, jpeg, png, gif, bmp 확장자(쿼리스트링 포함)로 끝나거나,
     * 2. src가 HTTP URL로 시작하고, 비디오/임베디드 관련 키워드(예: "youtube", "youtu.be", "vimeo", "dailymotion")가 포함되지 않으면 이미지로 판단한다.
     */
    private static boolean isImageUrl(String src) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
            return true;
        }
        return src.startsWith("http") &&
                !src.toLowerCase().contains("youtube") &&
                !src.toLowerCase().contains("youtu.be") &&
                !src.toLowerCase().contains("vimeo") &&
                !src.toLowerCase().contains("dailymotion");
    }
}
