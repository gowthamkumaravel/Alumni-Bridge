package com.aluminibackend.api.module.notification.service;

import com.aluminibackend.api.integration.notification.CreateNotificationCommand;
import com.aluminibackend.api.integration.notification.NotificationFeignClient;
import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.notification.dto.response.NotificationResponse;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Feign adapter for the notification write path. Contains no persistence logic:
 * it resolves the recipient's name/email snapshot from the monolith's users table
 * (user data has not been extracted) and delegates storage to notification-service
 * over Eureka-discovered Feign.
 *
 * <p>createNotificationsForAlumniCommunity stays here because it depends on
 * AlumniProfile, which remains in the monolith; it fans out one internal create
 * per opted-in recipient.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationFeignClient notificationFeignClient;
    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;

    @Override
    public NotificationResponse createSystemNotification(String employeeId,
                                                         String message,
                                                         NotificationCategory category) {
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationFeignClient.create(new CreateNotificationCommand(
                user.getEmployeeId(), user.getName(), user.getEmail(), message, category.name()));
    }

    @Override
    public void createNotificationsForAlumniCommunity(String message, NotificationCategory category) {
        alumniProfileRepository.findByCommunityOptInIs(true)
                .stream()
                .map(AlumniProfile::getUser)
                .forEach(user -> notificationFeignClient.create(new CreateNotificationCommand(
                        user.getEmployeeId(), user.getName(), user.getEmail(), message, category.name())));
    }
}
