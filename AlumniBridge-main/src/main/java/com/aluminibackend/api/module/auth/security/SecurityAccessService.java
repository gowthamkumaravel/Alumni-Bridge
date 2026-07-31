package com.aluminibackend.api.module.auth.security;

import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.alumnireferral.repository.AlumniReferralRepository;
import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Ownership/resource access checks for the monolith.
 *
 * <p>Migration note: the former canAccessNotification(...) method and its
 * NotificationRepository dependency were removed. Notification data now lives in
 * notification-service, which owns and enforces that ownership rule locally. This
 * eliminates the previous reverse dependency (auth core -> notification module).
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

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final AlumniReferralRepository alumniReferralRepository;
    private final OffboardingTaskRepository offboardingTaskRepository;

    public boolean canAccessUser(String employeeId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getEmployeeId)
                .map(currentEmployeeId -> currentEmployeeId.equals(employeeId))
                .orElse(false);
    }

    public boolean canAccessAlumniProfile(Long alumniId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return alumniProfileRepository.existsByAlumniIdAndUser_Email(
                alumniId, authentication.getName());
    }

    public boolean canAccessReferral(Long referralId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return alumniReferralRepository.existsByReferralIdAndAlumni_User_Email(
                referralId, authentication.getName());
    }

    public boolean canAccessAlumniReferrals(Long alumniId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return alumniReferralRepository.existsByAlumni_AlumniIdAndAlumni_User_Email(
                alumniId, authentication.getName());
    }

    public boolean canAccessTask(Long taskId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return offboardingTaskRepository.existsByTaskIdAndAssignedTo_Email(
                taskId, authentication.getName());
    }

    public boolean canAccessCaseTasks(Long caseId, Authentication authentication) {
        if (notAuthenticated(authentication)) {
            return false;
        }
        if (hasStaffAuthority(authentication)) {
            return true;
        }
        return offboardingTaskRepository.existsByOffboardingCase_CaseIdAndAssignedTo_Email(
                caseId, authentication.getName());
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
