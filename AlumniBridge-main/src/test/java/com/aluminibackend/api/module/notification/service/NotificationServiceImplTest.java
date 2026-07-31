package com.aluminibackend.api.module.notification.service;

import com.aluminibackend.api.integration.notification.CreateNotificationCommand;
import com.aluminibackend.api.integration.notification.NotificationFeignClient;
import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Feign-adapter tests: the monolith resolves user snapshots (name/email) and
 * delegates to notification-service, and the community broadcast fans out one
 * internal create per opted-in alumni user.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationFeignClient notificationFeignClient;
    @Mock private UserRepository userRepository;
    @Mock private AlumniProfileRepository alumniProfileRepository;

    @InjectMocks private NotificationServiceImpl service;

    @Test
    void createSystemNotification_resolvesSnapshotAndDelegates() {
        User user = User.builder()
                .employeeId("EMP042").name("Priya Nair").email("priya.nair@example.com").build();
        when(userRepository.findByEmployeeId("EMP042")).thenReturn(Optional.of(user));

        service.createSystemNotification("EMP042", "Case initiated", NotificationCategory.OFFBOARDING);

        ArgumentCaptor<CreateNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationFeignClient).create(captor.capture());
        CreateNotificationCommand sent = captor.getValue();
        assertThat(sent.getEmployeeId()).isEqualTo("EMP042");
        assertThat(sent.getUserName()).isEqualTo("Priya Nair");
        assertThat(sent.getUserEmail()).isEqualTo("priya.nair@example.com");
        assertThat(sent.getMessage()).isEqualTo("Case initiated");
        assertThat(sent.getCategory()).isEqualTo("OFFBOARDING");
    }

    @Test
    void createSystemNotification_missingUser_throws() {
        when(userRepository.findByEmployeeId("EMP099")).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                service.createSystemNotification("EMP099", "x", NotificationCategory.OFFBOARDING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        verifyNoInteractions(notificationFeignClient);
    }

    @Test
    void createNotificationsForAlumniCommunity_fansOutPerOptedInUser() {
        User u1 = User.builder().userId(1L).name("A").email("a@x.com").build();
        User u2 = User.builder().userId(2L).name("B").email("b@x.com").build();
        AlumniProfile p1 = mock(AlumniProfile.class);
        AlumniProfile p2 = mock(AlumniProfile.class);
        when(p1.getUser()).thenReturn(u1);
        when(p2.getUser()).thenReturn(u2);
        when(alumniProfileRepository.findByCommunityOptInIs(true)).thenReturn(List.of(p1, p2));

        service.createNotificationsForAlumniCommunity("Reunion published", NotificationCategory.ALUMNI_EVENT);

        verify(notificationFeignClient, times(2)).create(any(CreateNotificationCommand.class));
    }
}
