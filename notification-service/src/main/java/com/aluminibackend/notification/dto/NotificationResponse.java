package com.aluminibackend.notification.dto;

import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String employeeId;
    private String userName;
    private String message;
    private NotificationCategory category;
    private NotificationStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
