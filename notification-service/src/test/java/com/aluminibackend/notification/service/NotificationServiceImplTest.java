package com.aluminibackend.notification.service;

import com.aluminibackend.notification.dto.CreateNotificationRequest;
import com.aluminibackend.notification.dto.NotificationResponse;
import com.aluminibackend.notification.entity.Notification;
import com.aluminibackend.notification.enums.NotificationCategory;
import com.aluminibackend.notification.enums.NotificationStatus;
import com.aluminibackend.notification.mapper.NotificationMapper;
import com.aluminibackend.notification.repository.NotificationRepository;
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

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper mapper;

    @InjectMocks private NotificationServiceImpl service;

    @Test
    void createNotification_persistsSnapshotAndUnreadStatus() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId("EMP042");
        request.setUserName("Priya Nair");
        request.setUserEmail("priya.nair@example.com");
        request.setMessage("Your offboarding case has been initiated.");
        request.setCategory(NotificationCategory.OFFBOARDING);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Notification.class)))
                .thenReturn(NotificationResponse.builder().employeeId("EMP042").build());

        service.createNotification(request);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo("EMP042");
        assertThat(saved.getUserEmail()).isEqualTo("priya.nair@example.com");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    void markAsRead_setsReadStatus() {
        Notification n = Notification.builder()
                .notificationId(5L).employeeId("EMP042").status(NotificationStatus.UNREAD).build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(n)).thenReturn(n);
        when(mapper.toResponse(n)).thenReturn(NotificationResponse.builder().notificationId(5L).build());

        service.markAsRead(5L);

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    void getNotificationById_missing_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getNotificationById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    void getUnreadNotificationsByUser_delegatesToRepo() {
        when(notificationRepository.findByEmployeeIdAndStatus("EMP042", NotificationStatus.UNREAD))
                .thenReturn(List.of());
        service.getUnreadNotificationsByUser("EMP042");
        verify(notificationRepository).findByEmployeeIdAndStatus("EMP042", NotificationStatus.UNREAD);
    }
}
