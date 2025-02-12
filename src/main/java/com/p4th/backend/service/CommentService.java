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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(String postId) {
        List<Comment> comments = commentMapper.getCommentsByPost(postId);
        return comments.stream().map(CommentResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public String createComment(String postId, String userId, CommentCreateRequest request) {
        // 사용자 유효성 검사
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        Comment comment = new Comment();
        String commentId = ULIDUtil.getULID();
        comment.setCommentId(commentId);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setCreatedBy(userId);
        int inserted = commentMapper.insertComment(comment);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 등록 실패");
        }
        // 댓글 생성 후 해당 게시글의 comment_count 증가
        postMapper.incrementCommentCount(postId);
        return commentId;
    }

    @Transactional
    public boolean updateComment(String commentId, String content, String userId) {
        // 댓글 조회 및 권한 체크
        Comment comment = commentMapper.getCommentById(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글을 찾을 수 없습니다.");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        int updated = commentMapper.updateComment(commentId, content);
        return updated == 1;
    }

    @Transactional
    public boolean deleteComment(String commentId, String userId) {
        // 댓글 조회
        Comment comment = commentMapper.getCommentById(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글을 찾을 수 없습니다.");
        }
        // 권한 체크: 요청자가 댓글 작성자이거나, 관리자인 경우
        User requester = userMapper.selectByUserId(userId);
        if (!comment.getUserId().equals(userId) && (requester == null || requester.getAdminRole() != 1)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "권한이 없습니다.");
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
