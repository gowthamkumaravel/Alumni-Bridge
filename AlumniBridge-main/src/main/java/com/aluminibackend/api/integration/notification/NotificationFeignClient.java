package com.aluminibackend.api.integration.notification;

import com.aluminibackend.api.integration.FeignClientConfig;
import com.aluminibackend.api.module.notification.dto.response.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Discovery-based Feign client for notification-service.
 *
 * <p>No {@code url} — the target is resolved from Eureka by service name
 * ("notification-service") and load-balanced via Spring Cloud LoadBalancer.
 *
 * <p>Only the internal write endpoint is used from the monolith: system
 * notifications and alumni-community broadcasts. All public read/mutation traffic
 * is routed by the API gateway straight to notification-service, so those Feign
 * methods are no longer needed here.
 */
@FeignClient(
        name = "notification-service",
        configuration = FeignClientConfig.class
)
public interface NotificationFeignClient {

    @PostMapping("/internal/notifications")
    NotificationResponse create(@RequestBody CreateNotificationCommand command);
}
