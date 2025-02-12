package com.p4th.backend.service;

import com.p4th.backend.dto.response.SearchResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
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
            result.setCategory(post.getBoard() != null && post.getBoard().getCategory() != null ? post.getBoard().getCategory().getCategoryName() : null);
            result.setBoardName(post.getBoard() != null ? post.getBoard().getBoardName() : null);
            result.setViewCount(post.getViewCount());
            result.setCommentCount(post.getCommentCount());
            result.setImageUrl(post.getFirstAttachmentUrl());
            result.setImageCount(post.getAttachmentCount());
            result.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
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
            result.setCategory(post.getBoard() != null && post.getBoard().getCategory() != null ? post.getBoard().getCategory().getCategoryName() : null);
            result.setBoardName(post.getBoard() != null ? post.getBoard().getBoardName() : null);
            result.setViewCount(post.getViewCount());
            result.setCommentCount(post.getCommentCount());
            result.setImageUrl(post.getFirstAttachmentUrl());
            result.setImageCount(post.getAttachmentCount());
            result.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
            return result;
        });
    }
}
