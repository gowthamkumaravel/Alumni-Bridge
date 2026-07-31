package com.aluminibackend.notification.service;

import com.aluminibackend.notification.dto.CreateNotificationRequest;
import com.aluminibackend.notification.dto.NotificationResponse;
import com.aluminibackend.notification.dto.UpdateNotificationRequest;
import com.aluminibackend.notification.enums.NotificationCategory;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(Long notificationId);

    List<NotificationResponse> getAllNotifications();

    List<NotificationResponse> getNotificationsByUser(String employeeId);

    List<NotificationResponse> getUnreadNotificationsByUser(String employeeId);

    List<NotificationResponse> getNotificationsByCategory(NotificationCategory category);

    NotificationResponse updateNotification(Long notificationId, UpdateNotificationRequest request);

    NotificationResponse markAsRead(Long notificationId);

    NotificationResponse dismissNotification(Long notificationId);

    void deleteNotification(Long notificationId);
}
