package com.p4th.backend.service;

import com.p4th.backend.dto.response.SearchResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.repository.SearchRepository;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public Page<SearchResponse.SearchResult> search(String query, Pageable pageable) {
        // 전체 검색: 제목, 내용, 닉네임에 대해 대소문자 구분 없이 LIKE 검색
        Page<Post> posts = searchRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrUser_NicknameContainingIgnoreCase(
                query, query, query, pageable);
        return posts.map(post -> {
            SearchResponse.SearchResult result = new SearchResponse.SearchResult();
            result.setPostId(post.getPostId());
            result.setBoardId(post.getBoard() != null ? post.getBoard().getBoardId() : null);
            result.setUserId(post.getUser() != null ? post.getUser().getUserId() : null);
            result.setNickname(post.getUser() != null ? post.getUser().getNickname() : null);
            result.setTitle(post.getTitle());
            result.setCategory(post.getBoard() != null && post.getBoard().getCategory() != null
                    ? post.getBoard().getCategory().getCategoryName() : null);
            result.setBoardName(post.getBoard() != null ? post.getBoard().getBoardName() : null);
            result.setViewCount(post.getViewCount());
            result.setCommentCount(post.getCommentCount());
            // imageCount: HTML 내 이미지 태그(조건에 맞는 경우)의 개수
            result.setImageCount(countInlineImages(post.getContent()));
            result.setImageUrl(extractFirstImageUrl(post.getContent()));
            result.setCreatedAt(post.getCreatedAt() != null ? RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()) : null);
            return result;
        });
    }

    public Page<SearchResponse.SearchResult> searchInBoard(String boardId, String query, Pageable pageable) {
        Page<Post> posts = searchRepository.findByBoard_BoardIdAndTitleContainingIgnoreCaseOrBoard_BoardIdAndContentContainingIgnoreCaseOrBoard_BoardIdAndUser_NicknameContainingIgnoreCase(
                boardId, query,
                boardId, query,
                boardId, query,
                pageable);
        return posts.map(post -> {
            SearchResponse.SearchResult result = new SearchResponse.SearchResult();
            result.setPostId(post.getPostId());
            result.setBoardId(post.getBoard() != null ? post.getBoard().getBoardId() : null);
            result.setUserId(post.getUser() != null ? post.getUser().getUserId() : null);
            result.setNickname(post.getUser() != null ? post.getUser().getNickname() : null);
            result.setTitle(post.getTitle());
            result.setCategory(post.getBoard() != null && post.getBoard().getCategory() != null
                    ? post.getBoard().getCategory().getCategoryName() : null);
            result.setBoardName(post.getBoard() != null ? post.getBoard().getBoardName() : null);
            result.setViewCount(post.getViewCount());
            result.setCommentCount(post.getCommentCount());
            result.setImageCount(countInlineImages(post.getContent()));
            result.setImageUrl(extractFirstImageUrl(post.getContent()));
            result.setCreatedAt(post.getCreatedAt() != null ? RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()) : null);
            return result;
        });
    }

    /**
     * HTML 문자열에서 첫 번째 <img> 태그의 src 속성을 추출한다.
     * src가 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)로 끝거나,
     * 또는 HTTP URL로 시작하면서 (비디오/임베디드 관련 키워드가 없는 경우) 이미지 URL로 판단한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 첫 번째 이미지 URL (조건에 맞으면), 없으면 null
     */
    private static String extractFirstImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        Document doc = Jsoup.parse(htmlContent);
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
     * HTML 문자열 내의 <img> 태그 중, src가 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)로 끝거나,
     * 또는 HTTP URL로 시작하면서 (비디오/임베디드 관련 키워드가 없는 경우) 이미지로 판단되는 태그의 개수를 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 이미지 태그의 개수
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
            if (isImageUrl(src)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isImageUrl(String src) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        // 조건 1: 파일 확장자 검사
        if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
            return true;
        }
        // 조건 2: HTTP URL이면서 비디오/임베디드 관련 키워드가 없는 경우
        return src.startsWith("http") &&
                !src.toLowerCase().contains("youtube") &&
                !src.toLowerCase().contains("youtu.be") &&
                !src.toLowerCase().contains("vimeo") &&
                !src.toLowerCase().contains("dailymotion");
    }
}
