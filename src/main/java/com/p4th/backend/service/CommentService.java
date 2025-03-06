package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Comment;
import com.p4th.backend.domain.NotificationType;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.request.CommentCreateRequest;
import com.p4th.backend.dto.response.comment.CommentResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostMapper;
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
    private final AuthMapper authMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(String postId, String userId) {
        return commentMapper.getCommentsByPost(postId, userId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createComment(String postId, String userId, CommentCreateRequest request) {
        // 사용자 유효성 검사
        User user = authMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        // 게시글 존재 여부 확인
        Post post = postMapper.getPostDetail(postId, userId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Comment comment = new Comment();
        String commentId = ULIDUtil.getULID();
        comment.setCommentId(commentId);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setNickname(user.getNickname());
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setCreatedBy(userId);
        int inserted = commentMapper.insertComment(comment);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 등록 실패");
        }
        // 댓글 생성 후 해당 게시글의 comment_count 증가
        postMapper.incrementCommentCount(postId);

        // 알림 생성 로직
        // 1. 대댓글인 경우: 부모 댓글 작성자에게 알림 생성 (자신이 작성한 댓글은 제외)
        if (request.getParentCommentId() != null && !request.getParentCommentId().trim().isEmpty()) {
            Comment parentComment = commentMapper.getCommentById(request.getParentCommentId());
            if (parentComment != null && !userId.equals(parentComment.getUserId())) {
                notificationService.notifyComment(NotificationType.RECOMMENT, post, user, commentId, request.getContent());
            }
        } else {
            // 2. 일반 댓글인 경우: 게시글 작성자에게 알림 생성 (자신이 작성한 댓글은 제외)
            if (!userId.equals(post.getUserId())) {
                notificationService.notifyComment(NotificationType.COMMENT, post, user, commentId, request.getContent());
            }
        }

        return commentId;
    }

    @Transactional
    public boolean updateComment(String commentId, String content, String userId) {
        // 댓글 조회 및 권한 체크
        Comment comment = commentMapper.getCommentById(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        int updated = commentMapper.updateComment(commentId, content, userId);
        return updated == 1;
    }

    @Transactional
    public boolean deleteComment(String commentId, String userId) {
        // 댓글 조회
        Comment comment = commentMapper.getCommentById(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        // 권한 체크: 요청자가 댓글 작성자이거나, 관리자인 경우
        User requester = authMapper.selectByUserId(userId);
        if (!comment.getUserId().equals(userId) && (requester == null || requester.getAdminRole() != 1)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        int deleted = commentMapper.physicalDeleteComment(commentId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 삭제 실패");
        }
        // 댓글 삭제 후, 해당 게시글의 댓글 수를 재계산하여 업데이트
        int remainingCount = commentMapper.countCommentsByPost(comment.getPostId());
        postMapper.updateCommentCount(comment.getPostId(), remainingCount);
        return true;
    }
}
