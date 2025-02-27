package com.p4th.backend.mapper;

import com.p4th.backend.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    List<Notification> getNotificationsByUserId(@Param("userId") String userId);
    int updateNotificationRead(@Param("notificationId") String notificationId);
    int countUnreadNotifications(@Param("userId") String userId);
    int insertNotification(Notification notification);
}
