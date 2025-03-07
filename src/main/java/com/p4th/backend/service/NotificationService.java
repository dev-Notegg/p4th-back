package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.*;
import com.p4th.backend.dto.response.NotificationResponse;
import com.p4th.backend.dto.response.UnreadCountResponse;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.mapper.NotificationMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.HtmlContentUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final PostMapper postMapper;
    private final AuthMapper authMapper;
    @Autowired
    private MessageSource messageSource;

    Locale locale = Locale.getDefault();

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
            response.setTitle(notification.getTitle());
            response.setContent(notification.getContent());
            response.setReadYn(notification.getReadYn());
            response.setReadAt(notification.getReadAt());
            response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(notification.getCreatedAt()));

            // 게시글 ID가 있을 경우 이미지 URL 설정(삭제 안내는 제외)
            if (!notification.getType().equals(NotificationType.ALERT) && notification.getPostId() != null) {
                Post post = postMapper.getPostDetail(notification.getPostId(), null);
                if (post != null) {
                    response.setImageUrl(HtmlImageUtils.extractFirstImageUrl(post.getContent()));
                }
            }
            return response;
        }).collect(Collectors.toList());
    }

    private String getPostTitle(String postId) {
        Post post = postMapper.getPostDetail(postId, null);
        return post != null ? post.getTitle() : "";
    }

    @Transactional
    public boolean markNotificationAsRead(String notificationId) {
        int updated = notificationMapper.updateNotificationRead(notificationId);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "알림 읽음 처리 실패");
        }
        return true;
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userId) {
        int count = notificationMapper.countUnreadNotifications(userId);
        UnreadCountResponse response = new UnreadCountResponse();
        response.setUnreadCount(count);
        return response;
    }

    /**
     * 내 게시글에 댓글 혹은 대댓글이 달렸을 때 알림 생성
     */
    @Transactional
    public void notifyComment(NotificationType type, Post post, User user, String commentId, String commentContent) {
        Notification notification = new Notification();
        notification.setNotificationId(ULIDUtil.getULID());
        notification.setUserId(post.getUserId());
        notification.setPostId(post.getPostId());
        notification.setCommentId(commentId);
        notification.setType(type);
        if(type.equals(NotificationType.COMMENT)) {
            notification.setTitle(messageSource.getMessage("notification.comment", new Object[]{user.getNickname()}, locale));
        }else{
            notification.setTitle(messageSource.getMessage("notification.recomment", new Object[]{user.getNickname()}, locale));
        }
        notification.setContent(commentContent);
        notification.setReadYn(0);
        notification.setCreatedBy("SYSTEM");
        notificationMapper.insertNotification(notification);
    }

    /**
     * 공지 게시글이 등록되었을 때 모든 유저에게 알림 생성 (게시글 작성자는 제외)
     */
    @Transactional
    public void notifyNoticePost(String postId, String userId, String content) {
        List<String> userIds = authMapper.selectAllUserIds();
        for (String getUserId : userIds) {
            // 게시글 작성자는 알림 대상에서 제외
            if (getUserId.equals(userId)) {
                continue;
            }
            Notification notification = new Notification();
            notification.setNotificationId(ULIDUtil.getULID());
            notification.setUserId(getUserId);
            notification.setPostId(postId);
            notification.setCommentId(null);
            notification.setType(NotificationType.NOTICE);
            notification.setTitle(messageSource.getMessage("notification.notice", new Object[]{getPostTitle(notification.getPostId())}, locale));
            notification.setContent(HtmlContentUtils.extractPlainText(content, 30));
            notification.setReadYn(0);
            notification.setCreatedBy("SYSTEM");
            notificationMapper.insertNotification(notification);
        }
    }

    /**
     * 관리자가 게시글/댓글을 삭제한 경우 메세지 알림 설정
     */
    @Transactional
    public void notifyDeleteAlert(String division, String userId, String content) {
        Notification notification = new Notification();
        notification.setNotificationId(ULIDUtil.getULID());
        notification.setUserId(userId);
        notification.setType(NotificationType.ALERT);
        if(!division.equals("POST")){// 게시글 삭제 안내
            notification.setTitle(messageSource.getMessage("notification.delete", new Object[]{"게시글"}, locale));
            notification.setContent(
                    messageSource.getMessage("notification.alert.content",
                            new Object[]{"게시글", "게시글 제목", content},
                            locale)
            );
        }else{ // 댓글 삭제 안내
            notification.setTitle(messageSource.getMessage("notification.delete", new Object[]{"댓글"}, locale));
            notification.setContent(
                    messageSource.getMessage("notification.alert.content",
                            new Object[]{"댓글", "댓글 내용", content},
                            locale)
            );
        }
        notification.setReadYn(0);
        notification.setCreatedBy("SYSTEM");
        notificationMapper.insertNotification(notification);
    }
}
