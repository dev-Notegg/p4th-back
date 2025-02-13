package com.p4th.backend.service;

import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.dto.response.search.SearchResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.repository.SearchRepository;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public Page<SearchResponse.SearchResult> search(String boardId, String query, Pageable pageable) {
        Page<Post> posts;
        if(boardId == null) {
            posts = searchRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrUser_NicknameContainingIgnoreCase(
                    query, query, query, pageable);
        }else {
            posts = searchRepository.findByBoard_BoardIdAndTitleContainingIgnoreCaseOrBoard_BoardIdAndContentContainingIgnoreCaseOrBoard_BoardIdAndUser_NicknameContainingIgnoreCase(
                    boardId, query,
                    boardId, query,
                    boardId, query,
                    pageable);
        }
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
            if(!PostStatus.DELETED.equals(post.getStatus())) { //삭제된 게시글인 경우 이미지 처리하지 않음
                result.setImageCount(HtmlImageUtils.countInlineImages(post.getContent()));
                result.setImageUrl(HtmlImageUtils.extractFirstImageUrl(post.getContent()));
            }
            result.setCreatedAt(post.getCreatedAt() != null ? RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()) : null);
            return result;
        });
    }
}
