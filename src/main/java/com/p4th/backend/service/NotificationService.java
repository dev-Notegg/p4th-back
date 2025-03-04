package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Comment;
import com.p4th.backend.domain.Notification;
import com.p4th.backend.domain.Post;
import com.p4th.backend.dto.response.NotificationResponse;
import com.p4th.backend.dto.response.UnreadCountResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.NotificationMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final AuthMapper authMapper;


    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userId) {
        List<Notification> notifications = notificationMapper.getNotificationsByUserId(userId);
        return notifications.stream().map(notification -> {
            NotificationResponse response = new NotificationResponse();
            response.setNotificationId(notification.getNotificationId());
            response.setUserId(notification.getUserId());
            response.setPostId(notification.getPostId());
            response.setCommentId(notification.getCommentId());
            response.setType(notification.getType());
            response.setReadYn(notification.getReadYn());
            response.setReadAt(notification.getReadAt());
            response.setCreatedAt(notification.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(String notificationId) {
        int updated = notificationMapper.updateNotificationRead(notificationId);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "알림 읽음 처리 실패");
        }
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userId) {
        int count = notificationMapper.countUnreadNotifications(userId);
        UnreadCountResponse response = new UnreadCountResponse();
        response.setUnreadCount(count);
        return response;
    }

    /**
     * 내 게시글에 댓글이 달렸을 때 알림 생성
     */
    @Transactional
    public void notifyCommentOnMyPost(String postId, String userId, String commentId) {
        Post post = postMapper.getPostDetail(postId, userId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        // 자기 자신의 글에 달린 댓글은 알림 처리하지 않음
        if (userId.equals(post.getUserId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setNotificationId(ULIDUtil.getULID());
        notification.setUserId(post.getUserId());
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setType("COMMENT");
        notification.setReadYn(0);
        notification.setCreatedBy("SYSTEM");
        notificationMapper.insertNotification(notification);
    }

    /**
     * 내가 남긴 댓글에 대댓글이 달렸을 때 알림 생성
     */
    @Transactional
    public void notifyReplyOnMyComment(String parentCommentId, String replyCommentId, String replierId) {
        Comment parentComment = commentMapper.getCommentById(parentCommentId);
        if (parentComment == null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (replierId.equals(parentComment.getUserId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setNotificationId(ULIDUtil.getULID());
        notification.setUserId(parentComment.getUserId());
        notification.setPostId(parentComment.getPostId());
        notification.setCommentId(replyCommentId);
        notification.setType("RECOMMENT");
        notification.setReadYn(0);
        notification.setCreatedBy("SYSTEM");
        notificationMapper.insertNotification(notification);
    }

    /**
     * 공지 게시글이 등록되었을 때 모든 유저에게 알림 생성 (게시글 작성자는 제외)
     */
    @Transactional
    public void notifyNoticePost(String postId, String userId) {
        Post post = postMapper.getPostDetail(postId, userId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        List<String> userIds = authMapper.selectAllUserIds();
        for (String getUserId : userIds) {
            // 게시글 작성자는 알림 대상에서 제외
            if (getUserId.equals(post.getUserId())) {
                continue;
            }
            Notification notification = new Notification();
            notification.setNotificationId(ULIDUtil.getULID());
            notification.setUserId(getUserId);
            notification.setPostId(postId);
            notification.setCommentId(null);
            notification.setType("NOTICE");
            notification.setReadYn(0);
            notification.setCreatedBy("SYSTEM");
            notificationMapper.insertNotification(notification);
        }
    }
}
