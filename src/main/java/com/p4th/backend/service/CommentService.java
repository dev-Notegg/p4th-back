package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Comment;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.request.CommentCreateRequest;
import com.p4th.backend.dto.response.CommentResponse;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper; // 추가: 게시글 업데이트를 위해

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(String postId) {
        List<Comment> comments = commentMapper.getCommentsByPost(postId);
        return comments.stream().map(comment -> {
            CommentResponse response = new CommentResponse();
            response.setCommentId(comment.getCommentId());
            response.setParentCommentId(comment.getParentCommentId());
            response.setUserId(comment.getUserId());
            // 사용자 닉네임 조회
            User user = userMapper.selectByUserId(comment.getUserId());
            response.setNickname(user != null ? user.getNickname() : "");
            response.setContent(comment.getContent());
            response.setCreatedAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : null);
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public String createComment(String postId, CommentCreateRequest request) {
        // 사용자 유효성 검사
        User user = userMapper.selectByUserId(request.getUserId());
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        Comment comment = new Comment();
        String commentId = ULIDUtil.getULID();
        comment.setCommentId(commentId);
        comment.setPostId(postId);
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setCreatedBy(request.getUserId());
        int inserted = commentMapper.insertComment(comment);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 등록 실패");
        }
        // 댓글 생성 후 해당 게시글의 comment_count 증가
        postMapper.incrementCommentCount(postId);
        return commentId;
    }

    @Transactional
    public boolean updateComment(String commentId, String content) {
        int updated = commentMapper.updateComment(commentId, content);
        return updated == 1;
    }

    @Transactional
    public boolean deleteComment(String commentId) {
        // 먼저 삭제될 댓글의 postId를 확인
        Comment comment = commentMapper.getCommentById(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글을 찾을 수 없습니다.");
        }
        int deleted = commentMapper.deleteComment(commentId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 삭제 실패");
        }
        // 댓글 삭제 후 해당 게시글의 comment_count 감소
        postMapper.decrementCommentCount(comment.getPostId());
        return true;
    }
}
