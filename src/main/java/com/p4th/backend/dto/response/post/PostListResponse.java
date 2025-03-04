package com.p4th.backend.dto.response.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.util.HtmlContentUtils;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

@Data
public class PostListResponse {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String categoryName; // 게시판의 카테고리명
    private String boardName;    // 게시판명
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;
    private PostStatus status;
    private int viewCount;
    private int commentCount;
    private String imageUrl;    // 썸네일 이미지 URL
    private int imageCount;     // HTML 내 이미지 태그 개수
    private String createdAt;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostListResponse from(Post post) {
        PostListResponse dto = new PostListResponse();
        dto.postId = post.getPostId();
        dto.boardId = post.getBoardId();
        dto.userId = post.getUserId();
        if(post.getUser() != null) {
            dto.nickname = post.getUser().getNickname();
        }else{
            dto.nickname = post.getNickname() != null ? post.getNickname() : null;
        }
        dto.title = HtmlContentUtils.extractText(post.getTitle(), 23);
        if (post.getBoard() != null) {
            dto.boardName = post.getBoard().getBoardName();
            if (post.getBoard().getCategory() != null) {
                dto.categoryName = post.getBoard().getCategory().getCategoryName();
            }else{
                dto.categoryName = post.getBoard().getCategoryName() != null ? post.getBoard().getCategoryName() : null;
            }
        }
        dto.status = post.getStatus();
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();

        // HTML 태그 제거 후 순수 텍스트 추출 (최대 50자)
        dto.setContent(HtmlContentUtils.extractPlainText(post.getContent(), 50));

        // content가 DB에 이스케이프되어 저장된 경우 unescape 처리
        String unescapedContent = StringEscapeUtils.unescapeHtml4(post.getContent());

        // HTML 내 이미지 태그 처리
        dto.imageCount = countInlineImages(unescapedContent);
        dto.imageUrl = extractFirstImageUrl(unescapedContent);

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
        // img와 iframe 태그 모두 선택
        Elements elements = doc.select("img[src], iframe[src]");
        for (Element element : elements) {
            String src = element.attr("src");
            // 먼저 일반 이미지 URL 확인
            if (isImageUrl(src)) {
                return src;
            }
            // YouTube URL인 경우 영상 ID를 추출하여 썸네일 URL 반환
            if (src.toLowerCase().contains("youtube") || src.toLowerCase().contains("youtu.be")) {
                String videoId = extractYoutubeVideoId(src);
                if (videoId != null) {
                    return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                }
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

    /**
     * YouTube URL에서 영상 ID를 추출한다.
     */
    public static String extractYoutubeVideoId(String url) {
        // 정규표현식을 사용하여 영상 ID 추출
        Pattern pattern = Pattern.compile("(?<=v=|/embed/|youtu\\.be/)[^&\\n?#]+");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
