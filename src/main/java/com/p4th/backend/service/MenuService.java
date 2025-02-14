package com.p4th.backend.service;

import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Category;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.board.BoardResponse;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.dto.response.user.UserCommentPostResponse;
import com.p4th.backend.dto.response.user.UserProfileResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.MenuMapper;
import com.p4th.backend.repository.PostRepository;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final MenuMapper menuMapper;
    private final AuthMapper authMapper;

    @Transactional(readOnly = true)
    public Page<PostListResponse> getRecentPosts(String userId, Pageable pageable) {
        List<Post> recentPosts = postMapper.findRecentPostsByUserId(userId);
        List<PostListResponse> dtoList = recentPosts.stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    @Transactional(readOnly = true)
    public Page<PostListResponse> getUserPosts(String userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        List<PostListResponse> dtoList = posts.getContent().stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, posts.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<UserCommentPostResponse> getUserComments(String userId, Pageable pageable) {
        // userMapper.getCommentsByUser(userId)를 통해 사용자가 작성한 댓글들을 조회
        List<com.p4th.backend.domain.Comment> comments = menuMapper.getCommentsByUser(userId);
        Map<String, List<com.p4th.backend.domain.Comment>> grouped = comments.stream()
                .collect(Collectors.groupingBy(com.p4th.backend.domain.Comment::getPostId));

        List<UserCommentPostResponse> responses = grouped.entrySet().stream()
                .map(entry -> {
                    String postId = entry.getKey();
                    Post post = postMapper.getPostDetail(postId);
                    if (post == null) {
                        return null;
                    }
                    UserCommentPostResponse dto = UserCommentPostResponse.from(post);
                    List<com.p4th.backend.domain.Comment> myComments = entry.getValue().stream()
                            .filter(c -> c.getUserId().equals(userId))
                            .toList();
                    dto.setComments(myComments.stream()
                            .map(com.p4th.backend.dto.response.user.UserCommentResponse::from)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, responses.size());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return UserProfileResponse.from(user);
    }

    public List<Category> getAllCategories() {
        return menuMapper.getAllCategories();
    }

    public List<BoardResponse> getBoardsByCategory(String categoryId) {
        List<Board> boards = menuMapper.getBoardsByCategory(categoryId);
        return boards.stream().map(board -> {
            BoardResponse dto = new BoardResponse();
            dto.setBoardId(board.getBoardId());
            dto.setCategoryId(board.getCategoryId());
            dto.setCategoryName(board.getCategoryName());
            dto.setBoardName(board.getBoardName());
            dto.setBoardLevel(board.getBoardLevel());
            dto.setSortOrder(board.getSortOrder());
            dto.setRecommendYn(board.getRecommendYn());
            dto.setCreatedBy(board.getCreatedBy());
            dto.setCreatedAt(board.getCreatedAt());
            dto.setUpdatedBy(board.getUpdatedBy());
            dto.setUpdatedAt(board.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
