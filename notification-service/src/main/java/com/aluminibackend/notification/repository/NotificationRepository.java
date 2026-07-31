package com.aluminibackend.notification.repository;

import com.aluminibackend.notification.entity.Notification;
import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployeeId(String employeeId);

    List<Notification> findByEmployeeIdAndStatus(String employeeId, NotificationStatus status);

    List<Notification> findByCategory(NotificationCategory category);

    boolean existsByNotificationIdAndUserEmail(Long notificationId, String email);
}
