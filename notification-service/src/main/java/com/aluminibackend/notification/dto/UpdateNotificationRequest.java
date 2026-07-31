package com.aluminibackend.notification.dto;

import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.enums.NotificationStatus;
import lombok.Data;

@Data
public class UpdateNotificationRequest {

    private String message;

    private NotificationCategory category;

    private NotificationStatus status;
}
