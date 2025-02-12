package com.p4th.backend.service;

import com.p4th.backend.dto.response.SearchResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            // imageCount: HTML 내 이미지 태그(이미지 확장자인 경우) 개수만 계산
            result.setImageCount(countInlineImages(post.getContent()));
            // 썸네일 이미지 URL: HTML 내 첫 번째 <img> 태그의 src
            result.setImageUrl(extractThumbnail(post.getContent()));
            result.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : null);
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
            result.setImageUrl(extractThumbnail(post.getContent()));
            result.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : null);
            return result;
        });
    }

    /**
     * HTML 문자열에서 첫 번째 <img> 태그의 src 속성을 추출한다.
     * 단, src가 이미지 파일 확장자(jpg, jpeg, png, gif, bmp)인 경우에만 반환한다.
     *
     * @param htmlContent 게시글의 HTML 컨텐츠
     * @return 첫 번째 이미지 URL (이미지 확장자인 경우), 아니면 null
     */
    private static String extractThumbnail(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        Document doc = Jsoup.parse(htmlContent);
        Element img = doc.selectFirst("img[src]");
        if (img != null) {
            String src = img.attr("src");
            if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
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
    private static int countInlineImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return 0;
        }
        Document doc = Jsoup.parse(htmlContent);
        return doc.select("img[src]").stream()
                .filter(img -> img.attr("src").matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$"))
                .toArray().length;
    }
}
