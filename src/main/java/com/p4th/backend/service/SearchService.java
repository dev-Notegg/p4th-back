package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.dto.response.search.SearchResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.mapper.BlockMapper;
import com.p4th.backend.repository.SearchRepository;
import com.p4th.backend.util.HtmlContentUtils;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final BlockMapper blockMapper;

    public Page<SearchResponse.SearchResult> search(String boardId, String userId, String query, Pageable pageable) {
        try {
            Page<Post> posts;
            if (boardId == null || boardId.trim().isEmpty()) {
                posts = searchRepository.searchPosts(query, pageable);
            } else {
                posts = searchRepository.searchPostsByBoard(boardId, query, pageable);
            }

            // userId 존재하면, 차단된 작성자의 게시글을 필터링
            if (userId != null && !userId.trim().isEmpty()) {
                List<String> blockedUserIds = blockMapper.findBlockedByUserId(userId);
                List<Post> filteredPosts = posts.getContent().stream()
                        .filter(post -> post.getUserId().equals(userId) || !blockedUserIds.contains(post.getUserId()))
                        .collect(Collectors.toList());
                posts = new PageImpl<>(filteredPosts, pageable, filteredPosts.size());
            }

            return posts.map(post -> {
                SearchResponse.SearchResult result = new SearchResponse.SearchResult();
                result.setPostId(post.getPostId());
                result.setBoardId(post.getBoard() != null ? post.getBoard().getBoardId() : null);
                result.setUserId(post.getUser() != null ? post.getUser().getUserId() : null);
                result.setNickname(post.getUser() != null ? post.getUser().getNickname() : null);
                result.setTitle(HtmlContentUtils.extractText(post.getTitle(), 23));
                if (post.getBoard().getCategory() != null) {
                    result.setCategoryName(post.getBoard().getCategory().getCategoryName());
                }else{
                    result.setCategoryName(post.getBoard().getCategoryName() != null ? post.getBoard().getCategoryName() : null);
                }
                result.setBoardName(post.getBoard() != null ? post.getBoard().getBoardName() : null);
                result.setViewCount(post.getViewCount());
                result.setCommentCount(post.getCommentCount());
                result.setImageCount(HtmlImageUtils.countInlineImages(post.getContent()));
                result.setImageUrl(HtmlImageUtils.extractFirstImageUrl(post.getContent()));
                result.setCreatedAt(post.getCreatedAt() != null ?
                        RelativeTimeFormatter.formatRelativeTime(post.getCreatedAt()) : null);
                return result;
            });
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "검색 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
