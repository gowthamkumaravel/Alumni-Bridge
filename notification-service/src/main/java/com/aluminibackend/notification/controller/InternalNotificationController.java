package com.aluminibackend.notification.controller;

import com.aluminibackend.notification.dto.CreateNotificationRequest;
import com.aluminibackend.notification.dto.NotificationResponse;
import com.aluminibackend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Service-to-service write API. Reachable only with a valid X-Internal-Token
 * (enforced by InternalAuthFilter). Used by the monolith's Feign adapter for
 * system notifications and per-recipient alumni-community broadcasts.
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(request));
    }
}
