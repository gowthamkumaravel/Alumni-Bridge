package com.aluminibackend.api.module.alumnireferral.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.alumnireferral.dto.request.CreateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.request.UpdateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.response.AlumniReferralResponse;
import com.aluminibackend.api.module.alumnireferral.entity.AlumniReferral;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import com.aluminibackend.api.module.alumnireferral.mapper.AlumniReferralMapper;
import com.aluminibackend.api.module.alumnireferral.repository.AlumniReferralRepository;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlumniReferralServiceImplTest {

    @Mock
    private AlumniReferralRepository referralRepository;

    @Mock
    private AlumniProfileRepository alumniProfileRepository;

    @Mock
    private AlumniReferralMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AlumniReferralServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name) {
        return User.builder()
                .userId(userId)
                .employeeId("EMP" + userId)
                .name(name)
                .email(name.toLowerCase().replace(" ", "") + "@example.com")
                .build();
    }

    private AlumniProfile createAlumniProfile(Long alumniId, User user, String name) {
        return AlumniProfile.builder()
                .alumniId(alumniId)
                .user(user)
                .name(name)
                .formerEmployeeId("FORMER_" + alumniId)
                .build();
    }

    private AlumniReferral createAlumniReferral(Long referralId, AlumniProfile alumni, String name, String email, String jobReqId, AlumniReferralStatus status) {
        return AlumniReferral.builder()
                .referralId(referralId)
                .alumni(alumni)
                .candidateName(name)
                .candidateEmail(email)
                .jobRequisitionId(jobReqId)
                .referralDate(LocalDate.now())
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AlumniReferralResponse createAlumniReferralResponse(Long referralId, Long alumniId, String alumniName, String name, String email, String jobReqId, AlumniReferralStatus status) {
        return AlumniReferralResponse.builder()
                .referralId(referralId)
                .alumniId(alumniId)
                .alumniName(alumniName)
                .formerEmployeeId("FORMER_" + alumniId)
                .candidateName(name)
                .candidateEmail(email)
                .jobRequisitionId(jobReqId)
                .referralDate(LocalDate.now())
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateAlumniReferralRequest createCreateRequest(Long alumniId, String name, String email, String jobReqId) {
        CreateAlumniReferralRequest request = new CreateAlumniReferralRequest();
        request.setAlumniId(alumniId);
        request.setCandidateName(name);
        request.setCandidateEmail(email);
        request.setJobRequisitionId(jobReqId);
        return request;
    }

    private UpdateAlumniReferralRequest createUpdateRequest(String name, String email, String jobReqId, AlumniReferralStatus status) {
        UpdateAlumniReferralRequest request = new UpdateAlumniReferralRequest();
        request.setCandidateName(name);
        request.setCandidateEmail(email);
        request.setJobRequisitionId(jobReqId);
        request.setStatus(status);
        return request;
    }

    // --- createReferral Tests ---

    @Test
    void createReferral_Success() {
        CreateAlumniReferralRequest request = createCreateRequest(10L, "Jane Doe", "jane.doe@example.com", "JOB-101");
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral savedReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse expectedResponse = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101")).thenReturn(false);
        when(alumniProfileRepository.findById(10L)).thenReturn(Optional.of(alumni));
        when(referralRepository.save(any(AlumniReferral.class))).thenReturn(savedReferral);
        when(mapper.toResponse(savedReferral)).thenReturn(expectedResponse);

        AlumniReferralResponse response = service.createReferral(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReferral> referralCaptor = ArgumentCaptor.forClass(AlumniReferral.class);
        verify(referralRepository).save(referralCaptor.capture());
        AlumniReferral captured = referralCaptor.getValue();
        assertEquals(alumni, captured.getAlumni());
        assertEquals("Jane Doe", captured.getCandidateName());
        assertEquals("jane.doe@example.com", captured.getCandidateEmail());
        assertEquals("JOB-101", captured.getJobRequisitionId());
        assertEquals(AlumniReferralStatus.SUBMITTED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP1",
                "Your referral has been submitted for Jane Doe",
                NotificationCategory.REFERRAL
        );
        verify(auditLogService).record("EMP1","CREATE_ALUMNI_REFERRAL", "ALUMNI_REFERRAL");
        verify(referralRepository).existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101");
        verify(alumniProfileRepository).findById(10L);
        verify(mapper).toResponse(savedReferral);
    }

    @Test
    void createReferral_ThrowsException_WhenCandidateAlreadyReferred() {
        CreateAlumniReferralRequest request = createCreateRequest(10L, "Jane Doe", "jane.doe@example.com", "JOB-101");

        when(referralRepository.existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createReferral(request);
        });

        assertEquals("This candidate is already referred by the same alumni for this job", exception.getMessage());
        verify(referralRepository).existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101");
        verifyNoMoreInteractions(referralRepository);
        verifyNoInteractions(alumniProfileRepository, notificationService, auditLogService, mapper);
    }

    @Test
    void createReferral_ThrowsException_WhenAlumniProfileNotFound() {
        CreateAlumniReferralRequest request = createCreateRequest(10L, "Jane Doe", "jane.doe@example.com", "JOB-101");

        when(referralRepository.existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101")).thenReturn(false);
        when(alumniProfileRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createReferral(request);
        });

        assertEquals("Alumni profile not found", exception.getMessage());
        verify(referralRepository).existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(10L, "jane.doe@example.com", "JOB-101");
        verify(alumniProfileRepository).findById(10L);
        verifyNoMoreInteractions(referralRepository, alumniProfileRepository);
        verifyNoInteractions(notificationService, auditLogService, mapper);
    }

    // --- getReferralById Tests ---

    @Test
    void getReferralById_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse expectedResponse = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findById(100L)).thenReturn(Optional.of(referral));
        when(mapper.toResponse(referral)).thenReturn(expectedResponse);

        AlumniReferralResponse response = service.getReferralById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(referralRepository).findById(100L);
        verify(mapper).toResponse(referral);
    }

    @Test
    void getReferralById_ThrowsException_WhenNotFound() {
        when(referralRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getReferralById(100L);
        });

        assertEquals("Alumni referral not found with ID: 100", exception.getMessage());
        verify(referralRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getAllReferrals Tests ---

    @Test
    void getAllReferrals_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse response = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findAll()).thenReturn(Collections.singletonList(referral));
        when(mapper.toResponse(referral)).thenReturn(response);

        List<AlumniReferralResponse> responses = service.getAllReferrals();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(referralRepository).findAll();
        verify(mapper).toResponse(referral);
    }

    // --- getReferralsByAlumni Tests ---

    @Test
    void getReferralsByAlumni_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse response = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findByAlumni_AlumniId(10L)).thenReturn(Collections.singletonList(referral));
        when(mapper.toResponse(referral)).thenReturn(response);

        List<AlumniReferralResponse> responses = service.getReferralsByAlumni(10L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(referralRepository).findByAlumni_AlumniId(10L);
        verify(mapper).toResponse(referral);
    }

    // --- getReferralsByCandidateEmail Tests ---

    @Test
    void getReferralsByCandidateEmail_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse response = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findByCandidateEmail("jane.doe@example.com")).thenReturn(Collections.singletonList(referral));
        when(mapper.toResponse(referral)).thenReturn(response);

        List<AlumniReferralResponse> responses = service.getReferralsByCandidateEmail("jane.doe@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(referralRepository).findByCandidateEmail("jane.doe@example.com");
        verify(mapper).toResponse(referral);
    }

    // --- getReferralsByJob Tests ---

    @Test
    void getReferralsByJob_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse response = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findByJobRequisitionId("JOB-101")).thenReturn(Collections.singletonList(referral));
        when(mapper.toResponse(referral)).thenReturn(response);

        List<AlumniReferralResponse> responses = service.getReferralsByJob("JOB-101");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(referralRepository).findByJobRequisitionId("JOB-101");
        verify(mapper).toResponse(referral);
    }

    // --- getReferralsByStatus Tests ---

    @Test
    void getReferralsByStatus_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral referral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse response = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findByStatus(AlumniReferralStatus.SUBMITTED)).thenReturn(Collections.singletonList(referral));
        when(mapper.toResponse(referral)).thenReturn(response);

        List<AlumniReferralResponse> responses = service.getReferralsByStatus(AlumniReferralStatus.SUBMITTED);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(referralRepository).findByStatus(AlumniReferralStatus.SUBMITTED);
        verify(mapper).toResponse(referral);
    }

    // --- updateReferral Tests ---

    @Test
    void updateReferral_Success_AllFieldsProvided() {
        UpdateAlumniReferralRequest request = createUpdateRequest("Jane Smith", "jane.smith@example.com", "JOB-202", AlumniReferralStatus.HIRED);
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral existingReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferral updatedReferral = createAlumniReferral(100L, alumni, "Jane Smith", "jane.smith@example.com", "JOB-202", AlumniReferralStatus.HIRED);
        AlumniReferralResponse expectedResponse = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Smith", "jane.smith@example.com", "JOB-202", AlumniReferralStatus.HIRED);

        when(referralRepository.findById(100L)).thenReturn(Optional.of(existingReferral));
        when(referralRepository.save(existingReferral)).thenReturn(updatedReferral);
        when(mapper.toResponse(updatedReferral)).thenReturn(expectedResponse);

        AlumniReferralResponse response = service.updateReferral(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReferral> referralCaptor = ArgumentCaptor.forClass(AlumniReferral.class);
        verify(referralRepository).save(referralCaptor.capture());
        AlumniReferral captured = referralCaptor.getValue();
        assertEquals("Jane Smith", captured.getCandidateName());
        assertEquals("jane.smith@example.com", captured.getCandidateEmail());
        assertEquals("JOB-202", captured.getJobRequisitionId());
        assertEquals(AlumniReferralStatus.HIRED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP1",
                "Your referral has been updated: HIRED",
                NotificationCategory.REFERRAL
        );
        verify(auditLogService).record("EMP1","UPDATE_ALUMNI_REFERRAL", "ALUMNI_REFERRAL");
        verify(referralRepository).findById(100L);
        verify(mapper).toResponse(updatedReferral);
    }

    @Test
    void updateReferral_Success_NullFieldsProvided() {
        UpdateAlumniReferralRequest request = createUpdateRequest(null, null, null, null);
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral existingReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferralResponse expectedResponse = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findById(100L)).thenReturn(Optional.of(existingReferral));
        when(referralRepository.save(existingReferral)).thenReturn(existingReferral);
        when(mapper.toResponse(existingReferral)).thenReturn(expectedResponse);

        AlumniReferralResponse response = service.updateReferral(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReferral> referralCaptor = ArgumentCaptor.forClass(AlumniReferral.class);
        verify(referralRepository).save(referralCaptor.capture());
        AlumniReferral captured = referralCaptor.getValue();
        assertEquals("Jane Doe", captured.getCandidateName());
        assertEquals("jane.doe@example.com", captured.getCandidateEmail());
        assertEquals("JOB-101", captured.getJobRequisitionId());
        assertEquals(AlumniReferralStatus.SUBMITTED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP1",
                "Your referral has been updated: SUBMITTED",
                NotificationCategory.REFERRAL
        );
        verify(auditLogService).record("EMP1","UPDATE_ALUMNI_REFERRAL", "ALUMNI_REFERRAL");
        verify(referralRepository).findById(100L);
        verify(mapper).toResponse(existingReferral);
    }

    @Test
    void updateReferral_ThrowsException_WhenNotFound() {
        UpdateAlumniReferralRequest request = createUpdateRequest("Jane Smith", "jane.smith@example.com", "JOB-202", AlumniReferralStatus.HIRED);

        when(referralRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateReferral(100L, request);
        });

        assertEquals("Alumni referral not found with ID: 100", exception.getMessage());
        verify(referralRepository).findById(100L);
        verifyNoMoreInteractions(referralRepository);
        verifyNoInteractions(notificationService, auditLogService, mapper);
    }

    // --- updateStatus Tests ---

    @Test
    void updateStatus_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral existingReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);
        AlumniReferral updatedReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.CONTACTED);
        AlumniReferralResponse expectedResponse = createAlumniReferralResponse(100L, 10L, "Alumni Referrer", "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.CONTACTED);

        when(referralRepository.findById(100L)).thenReturn(Optional.of(existingReferral));
        when(referralRepository.save(existingReferral)).thenReturn(updatedReferral);
        when(mapper.toResponse(updatedReferral)).thenReturn(expectedResponse);

        AlumniReferralResponse response = service.updateStatus(100L, AlumniReferralStatus.CONTACTED);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReferral> referralCaptor = ArgumentCaptor.forClass(AlumniReferral.class);
        verify(referralRepository).save(referralCaptor.capture());
        AlumniReferral captured = referralCaptor.getValue();
        assertEquals(AlumniReferralStatus.CONTACTED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP1",
                "Your referral status is now: CONTACTED",
                NotificationCategory.REFERRAL
        );
        verify(auditLogService).record("EMP1","UPDATE_ALUMNI_REFERRAL_STATUS", "ALUMNI_REFERRAL");
        verify(referralRepository).findById(100L);
        verify(mapper).toResponse(updatedReferral);
    }

    @Test
    void updateStatus_ThrowsException_WhenNotFound() {
        when(referralRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateStatus(100L, AlumniReferralStatus.CONTACTED);
        });

        assertEquals("Alumni referral not found with ID: 100", exception.getMessage());
        verify(referralRepository).findById(100L);
        verifyNoMoreInteractions(referralRepository);
        verifyNoInteractions(notificationService, auditLogService, mapper);
    }

    // --- deleteReferral Tests ---

    @Test
    void deleteReferral_Success() {
        User user = createUser(1L, "Alumni Referrer");
        AlumniProfile alumni = createAlumniProfile(10L, user, "Alumni Referrer");
        AlumniReferral existingReferral = createAlumniReferral(100L, alumni, "Jane Doe", "jane.doe@example.com", "JOB-101", AlumniReferralStatus.SUBMITTED);

        when(referralRepository.findById(100L)).thenReturn(Optional.of(existingReferral));
        doNothing().when(referralRepository).delete(existingReferral);

        assertDoesNotThrow(() -> {
            service.deleteReferral(100L);
        });

        verify(referralRepository).findById(100L);
        verify(referralRepository).delete(existingReferral);
    }

    @Test
    void deleteReferral_ThrowsException_WhenNotFound() {
        when(referralRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteReferral(100L);
        });

        assertEquals("Alumni referral not found with ID: 100", exception.getMessage());
        verify(referralRepository).findById(100L);
        verifyNoMoreInteractions(referralRepository);
    }
}
