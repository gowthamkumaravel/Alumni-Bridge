package com.aluminibackend.notification.controller;

import com.aluminibackend.notification.dto.CreateNotificationRequest;
import com.aluminibackend.notification.dto.NotificationResponse;
import com.aluminibackend.notification.dto.UpdateNotificationRequest;
import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public read/mutation API. Same paths and @PreAuthorize rules as the original
 * monolith controller. Creation is handled via the internal API (the monolith
 * resolves user snapshots), so there is no public POST here.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Public create endpoint, preserving the monolith's original
     * {@code POST /api/notifications}. userName/userEmail snapshots are optional
     * here; system-generated notifications supply them via the internal endpoint.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @PreAuthorize("@securityAccessService.canAccessNotification(#notificationId, authentication)")
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getNotificationById(notificationId));
    }

    @PreAuthorize("@securityAccessService.canAccessUser(#employeeId, authentication)")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(@PathVariable String employeeId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(employeeId));
    }

    @PreAuthorize("@securityAccessService.canAccessUser(#employeeId, authentication)")
    @GetMapping("/employee/{employeeId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsByUser(@PathVariable String employeeId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUser(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByCategory(
            @PathVariable NotificationCategory category) {
        return ResponseEntity.ok(notificationService.getNotificationsByCategory(category));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable Long notificationId,
            @Valid @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.updateNotification(notificationId, request));
    }

    @PreAuthorize("@securityAccessService.canAccessNotification(#notificationId, authentication)")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PreAuthorize("@securityAccessService.canAccessNotification(#notificationId, authentication)")
    @PatchMapping("/{notificationId}/dismiss")
    public ResponseEntity<NotificationResponse> dismissNotification(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.dismissNotification(notificationId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
