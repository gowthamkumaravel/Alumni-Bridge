package com.aluminibackend.notification.dto;

import com.aluminibackend.notification.enums.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Internal write payload. The monolith resolves userName/userEmail snapshots
 * from its own users table before calling, so this service never joins users.
 */
@Data
public class CreateNotificationRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private String userName;

    private String userEmail;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Category is required")
    private NotificationCategory category;
}
