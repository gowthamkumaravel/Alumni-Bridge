package com.aluminibackend.notification.service;

import com.aluminibackend.notification.dto.CreateNotificationRequest;
import com.aluminibackend.notification.dto.NotificationResponse;
import com.aluminibackend.notification.dto.UpdateNotificationRequest;
import com.aluminibackend.notification.entity.Notification;
import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.enums.NotificationStatus;
import com.aluminibackend.notification.mapper.NotificationMapper;
import com.aluminibackend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic preserved from the monolith's NotificationServiceImpl.
 * Removed: the User lookup and the alumni-community broadcast (which depended on
 * AlumniProfileRepository). Community broadcast now runs in the monolith, which
 * resolves recipients and calls createNotification per recipient.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .employeeId(request.getEmployeeId())
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .message(request.getMessage())
                .category(request.getCategory())
                .status(NotificationStatus.UNREAD)
                .build();

        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long notificationId) {
        return mapper.toResponse(findNotificationById(notificationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUser(String employeeId) {
        return notificationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsByUser(String employeeId) {
        return notificationRepository.findByEmployeeIdAndStatus(employeeId, NotificationStatus.UNREAD)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCategory(NotificationCategory category) {
        return notificationRepository.findByCategory(category)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse updateNotification(Long notificationId, UpdateNotificationRequest request) {
        Notification notification = findNotificationById(notificationId);

        if (request.getMessage() != null) {
            notification.setMessage(request.getMessage());
        }
        if (request.getCategory() != null) {
            notification.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            notification.setStatus(request.getStatus());
        }

        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notification.setStatus(NotificationStatus.READ);
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse dismissNotification(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notification.setStatus(NotificationStatus.DISMISSED);
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.delete(findNotificationById(notificationId));
    }

    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException(
                        "Notification not found with ID: " + notificationId));
    }
}
