package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Comment;
import com.p4th.backend.domain.CommentStatus;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.p4th.backend.util.RelativeTimeFormatter.formatRelativeTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final AuthMapper authMapper;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(String postId) {
        // 모든 댓글을 가져오되, 나중에 상태에 따라 필터링 또는 내용 변경 처리
        List<Comment> comments = commentMapper.getCommentsByPost(postId);
        // 그룹핑: 부모 댓글 ID별 자식 댓글 목록을 생성 (자식 댓글이 있는 경우 표시)
        Map<String, List<Comment>> childrenMap = comments.stream()
                .filter(c -> c.getParentCommentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentCommentId));

        // 댓글 리스트 변환: 삭제 상태인 댓글 중 자식 댓글이 없으면 제외
        return comments.stream()
                .filter(c -> {
                    if (CommentStatus.DELETED.equals(c.getStatus())) {
                        // 자식 댓글이 있는지 확인
                        return childrenMap.containsKey(c.getCommentId());
                    }
                    return true;
                })
                .map(comment -> {
                    // 만약 댓글 상태가 DELETED이면, content를 "삭제된 댓글입니다"로 설정
                    if (CommentStatus.DELETED.equals(comment.getStatus())) {
                        comment.setContent("삭제된 댓글입니다");
                    }
                    // 작성일시 변환
                    String relativeTime = comment.getCreatedAt() != null ? formatRelativeTime(comment.getCreatedAt()) : null;
                    CommentResponse response = CommentResponse.from(comment);
                    response.setCreatedAt(relativeTime);
                    return response;
                })
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
        Post post = postMapper.getPostDetail(postId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
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
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (CommentStatus.DELETED.equals(comment.getStatus())) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
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
        // 이미 삭제 상태인 경우 에러 발생
        if (CommentStatus.DELETED.equals(comment.getStatus())) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }
        // 권한 체크: 요청자가 댓글 작성자이거나, 관리자인 경우
        User requester = authMapper.selectByUserId(userId);
        if (!comment.getUserId().equals(userId) && (requester == null || requester.getAdminRole() != 1)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        // 자식 댓글이 있는지 확인
        int childCount = commentMapper.countChildComments(commentId);
        if (childCount == 0) {
            // 자식 댓글이 없으면 물리 삭제
            int deleted = commentMapper.physicalDeleteComment(commentId);
            if (deleted != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 삭제 실패");
            }
        } else {
            // 자식 댓글이 있으면 상태 업데이트 처리
            int updated = commentMapper.deleteComment(commentId, userId);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 삭제 실패");
            }
        }
        // 댓글 삭제 후 해당 게시글의 comment_count 감소
        postMapper.decrementCommentCount(comment.getPostId());
        return true;
    }
}
