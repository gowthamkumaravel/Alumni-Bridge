package com.aluminibackend.api.integration.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent to notification-service's internal create endpoint. Carries the
 * recipient's id plus name/email snapshots resolved by the monolith. The category
 * is sent as its enum name (String) and matched by name on the service side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationCommand {

    private String employeeId;
    private String userName;
    private String userEmail;
    private String message;
    private String category;
}
