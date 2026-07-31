package com.aluminibackend.api.module.notification.service;

import com.aluminibackend.api.module.notification.dto.response.NotificationResponse;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;

/**
 * Monolith-side notification contract.
 *
 * <p>After the microservice extraction this interface exposes only the write
 * operations that other monolith modules actually invoke. All read/query and
 * state-change operations now live in notification-service and are reached by
 * clients directly through the API gateway. The single Spring bean implementing
 * this interface is a Feign adapter (no local persistence).
 */
public interface NotificationService {

    NotificationResponse createSystemNotification(
            String employeeId,
            String message,
            NotificationCategory category
    );

    void createNotificationsForAlumniCommunity(
            String message,
            NotificationCategory category
    );
}
