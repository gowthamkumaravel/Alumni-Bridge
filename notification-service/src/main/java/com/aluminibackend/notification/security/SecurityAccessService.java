package com.aluminibackend.notification.security;

import com.aluminibackend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Ownership/authorization logic moved here from the monolith's SecurityAccessService.
 * This removes the former reverse dependency (auth core -> NotificationRepository):
 * the notification service now owns the repository and enforces its own access rules.
 */
@Component("securityAccessService")
@RequiredArgsConstructor
public class SecurityAccessService {

    private static final Set<String> STAFF_AUTHORITIES = Set.of(
            "ROLE_ADMIN",
            "ROLE_HRBP",
            "ROLE_IT_OPS",
            "ROLE_ALUMNI_RELATIONS",
            "ROLE_TALENT_ACQUISITION"
    );

    private final NotificationRepository notificationRepository;

    public boolean canAccessUser(String employeeId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        Object principal = authentication.getPrincipal();
        return (principal instanceof AuthUser au)
                && au.getEmployeeId() != null
                && au.getEmployeeId().equals(employeeId);
    }

    public boolean canAccessNotification(Long notificationId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return notificationRepository.existsByNotificationIdAndUserEmail(
                notificationId, authentication.getName());
    }

    private boolean notAuthenticated(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated();
    }

    private boolean hasStaffAuthority(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF_AUTHORITIES::contains);
    }
}
