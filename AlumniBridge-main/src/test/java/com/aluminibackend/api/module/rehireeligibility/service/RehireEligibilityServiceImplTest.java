package com.aluminibackend.api.module.rehireeligibility.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.rehireeligibility.dto.request.CreateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.request.UpdateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.response.RehireEligibilityResponse;
import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import com.aluminibackend.api.module.rehireeligibility.mapper.RehireEligibilityMapper;
import com.aluminibackend.api.module.rehireeligibility.repository.RehireEligibilityRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RehireEligibilityServiceImplTest {

    @Mock
    private RehireEligibilityRepository eligibilityRepository;

    @Mock
    private AlumniProfileRepository alumniProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RehireEligibilityMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RehireEligibilityServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name, UserRole role) {
        return User.builder()
                .userId(userId)
                .name(name)
                .role(role)
                .email(name.toLowerCase().replace(" ", "") + "@example.com")
                .employeeId("EMP" + userId)
                .password("password")
                .build();
    }

    private AlumniProfile createAlumniProfile(Long alumniId, User user) {
        return AlumniProfile.builder()
                .alumniId(alumniId)
                .user(user)
                .name("Alumni " + alumniId)
                .formerEmployeeId("FORMER_" + alumniId)
                .build();
    }

    private RehireEligibility createEligibilityEntity(Long id, AlumniProfile alumni, User reviewer, EligibleForRehire eligibleForRehire, RehireEligibilityStatus status) {
        return RehireEligibility.builder()
                .eligibilityId(id)
                .alumni(alumni)
                .eligibleForRehire(eligibleForRehire)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.now())
                .eligibilityReason("Reason")
                .reviewedBy(reviewer)
                .status(status)
                .build();
    }

    private RehireEligibilityResponse createEligibilityResponse(Long id, Long alumniId, Long reviewerId, EligibleForRehire eligibleForRehire, RehireEligibilityStatus status) {
        return RehireEligibilityResponse.builder()
                .eligibilityId(id)
                .alumniId(alumniId)
                .eligibleForRehire(eligibleForRehire)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.now())
                .eligibilityReason("Reason")
                .reviewedById(reviewerId)
                .status(status)
                .build();
    }

    // --- createEligibility Tests ---

    @Test
    void createEligibility_ThrowsException_WhenEligibilityAlreadyExists() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createEligibility(request);
        });

        assertEquals("Rehire eligibility already exists for this alumni", exception.getMessage());
        verify(eligibilityRepository).existsByAlumni_AlumniId(1L);
        verifyNoMoreInteractions(eligibilityRepository);
        verifyNoInteractions(alumniProfileRepository, userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createEligibility_ThrowsException_WhenAlumniProfileNotFound() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createEligibility(request);
        });

        assertEquals("Alumni profile not found", exception.getMessage());
        verify(eligibilityRepository).existsByAlumni_AlumniId(1L);
        verify(alumniProfileRepository).findById(1L);
        verifyNoMoreInteractions(eligibilityRepository, alumniProfileRepository);
        verifyNoInteractions(userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createEligibility_ThrowsException_WhenReviewerNotFound() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createEligibility(request);
        });

        assertEquals("Reviewer user not found", exception.getMessage());
        verify(eligibilityRepository).existsByAlumni_AlumniId(1L);
        verify(alumniProfileRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(eligibilityRepository, alumniProfileRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    @Test
    void createEligibility_ThrowsException_WhenReviewerRoleInvalid() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Employee Reviewer", UserRole.EMPLOYEE);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createEligibility(request);
        });

        assertEquals("Only ADMIN, HRBP or TALENT_ACQUISITION can review rehire eligibility", exception.getMessage());
        verify(eligibilityRepository).existsByAlumni_AlumniId(1L);
        verify(alumniProfileRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(eligibilityRepository, alumniProfileRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    @Test
    void createEligibility_Success_AllFieldsProvided_AdminReviewer() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);
        request.setEligibleForRehire(EligibleForRehire.YES);
        request.setCoolingOffPeriodMonths(6);
        request.setEligibilityReviewDate(LocalDate.of(2026, 1, 1));
        request.setEligibilityReason("Good performance");
        request.setStatus(RehireEligibilityStatus.ELIGIBLE);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.YES)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.of(2026, 1, 1))
                .eligibilityReason("Good performance")
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.ELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(eligibilityRepository.save(any(RehireEligibility.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.createEligibility(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(alumni, captured.getAlumni());
        assertEquals(EligibleForRehire.YES, captured.getEligibleForRehire());
        assertEquals(6, captured.getCoolingOffPeriodMonths());
        assertEquals(LocalDate.of(2026, 1, 1), captured.getEligibilityReviewDate());
        assertEquals("Good performance", captured.getEligibilityReason());
        assertEquals(reviewer, captured.getReviewedBy());
        assertEquals(RehireEligibilityStatus.ELIGIBLE, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your rehire eligibility has been reviewed: ELIGIBLE",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "CREATE_REHIRE_ELIGIBILITY", "REHIRE_ELIGIBILITY");
        verify(mapper).toResponse(expectedSaved);
    }

    @Test
    void createEligibility_Success_DefaultsAndResolveStatus_YES_HrbpReviewer() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);
        request.setEligibleForRehire(EligibleForRehire.YES);
        // coolingOffPeriodMonths is null, eligibilityReviewDate is null, status is null

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "HRBP Reviewer", UserRole.HRBP);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.YES)
                .coolingOffPeriodMonths(null)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.ELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(eligibilityRepository.save(any(RehireEligibility.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.createEligibility(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(alumni, captured.getAlumni());
        assertEquals(EligibleForRehire.YES, captured.getEligibleForRehire());
        assertNull(captured.getCoolingOffPeriodMonths());
        assertEquals(LocalDate.now(), captured.getEligibilityReviewDate()); // defaults to LocalDate.now()
        assertEquals(reviewer, captured.getReviewedBy());
        assertEquals(RehireEligibilityStatus.ELIGIBLE, captured.getStatus()); // resolved status
    }

    @Test
    void createEligibility_Success_ResolveStatus_NO_TaReviewer() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);
        request.setEligibleForRehire(EligibleForRehire.NO);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "TA Reviewer", UserRole.TALENT_ACQUISITION);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.NO)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.INELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.NO, RehireEligibilityStatus.INELIGIBLE);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(eligibilityRepository.save(any(RehireEligibility.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.createEligibility(request);

        assertNotNull(response);
        assertEquals(RehireEligibilityStatus.INELIGIBLE, expectedSaved.getStatus()); // resolved status
    }

    @Test
    void createEligibility_Success_ResolveStatus_CONDITIONAL() {
        CreateRehireEligibilityRequest request = new CreateRehireEligibilityRequest();
        request.setAlumniId(1L);
        request.setReviewedById(2L);
        request.setEligibleForRehire(EligibleForRehire.CONDITIONAL);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.CONDITIONAL)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.UNDER_REVIEW)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.CONDITIONAL, RehireEligibilityStatus.UNDER_REVIEW);

        when(eligibilityRepository.existsByAlumni_AlumniId(1L)).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(eligibilityRepository.save(any(RehireEligibility.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.createEligibility(request);

        assertNotNull(response);
        assertEquals(RehireEligibilityStatus.UNDER_REVIEW, expectedSaved.getStatus()); // resolved status
    }

    // --- getEligibilityById Tests ---

    @Test
    void getEligibilityById_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.getEligibilityById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(eligibilityRepository).findById(100L);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getEligibilityById_ThrowsException_WhenNotFound() {
        when(eligibilityRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getEligibilityById(100L);
        });

        assertEquals("Rehire eligibility not found with ID: 100", exception.getMessage());
        verify(eligibilityRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getEligibilityByAlumni Tests ---

    @Test
    void getEligibilityByAlumni_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.getEligibilityByAlumni(1L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(eligibilityRepository).findByAlumni_AlumniId(1L);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getEligibilityByAlumni_ThrowsException_WhenNotFound() {
        when(eligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getEligibilityByAlumni(1L);
        });

        assertEquals("Rehire eligibility not found for alumni ID: 1", exception.getMessage());
        verify(eligibilityRepository).findByAlumni_AlumniId(1L);
        verifyNoInteractions(mapper);
    }

    // --- getAllEligibilities Tests ---

    @Test
    void getAllEligibilities_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findAll()).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        List<RehireEligibilityResponse> responses = service.getAllEligibilities();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(eligibilityRepository).findAll();
        verify(mapper).toResponse(entity);
    }

    // --- getByEligibleForRehire Tests ---

    @Test
    void getByEligibleForRehire_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findByEligibleForRehire(EligibleForRehire.YES)).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        List<RehireEligibilityResponse> responses = service.getByEligibleForRehire(EligibleForRehire.YES);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(eligibilityRepository).findByEligibleForRehire(EligibleForRehire.YES);
    }

    // --- getByStatus Tests ---

    @Test
    void getByStatus_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findByStatus(RehireEligibilityStatus.ELIGIBLE)).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        List<RehireEligibilityResponse> responses = service.getByStatus(RehireEligibilityStatus.ELIGIBLE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(eligibilityRepository).findByStatus(RehireEligibilityStatus.ELIGIBLE);
    }

    // --- getByReviewer Tests ---

    @Test
    void getByReviewer_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility entity = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findByReviewedBy_UserId(2L)).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        List<RehireEligibilityResponse> responses = service.getByReviewer(2L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(eligibilityRepository).findByReviewedBy_UserId(2L);
    }

    // --- updateEligibility Tests ---

    @Test
    void updateEligibility_ThrowsException_WhenReviewerNotFound() {
        UpdateRehireEligibilityRequest request = new UpdateRehireEligibilityRequest();
        request.setReviewedById(99L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateEligibility(100L, request);
        });

        assertEquals("Reviewer user not found", exception.getMessage());
        verify(eligibilityRepository).findById(100L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(eligibilityRepository, userRepository);
    }

    @Test
    void updateEligibility_ThrowsException_WhenReviewerRoleInvalid() {
        UpdateRehireEligibilityRequest request = new UpdateRehireEligibilityRequest();
        request.setReviewedById(99L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        User invalidReviewer = createUser(99L, "Employee Reviewer", UserRole.EMPLOYEE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(99L)).thenReturn(Optional.of(invalidReviewer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateEligibility(100L, request);
        });

        assertEquals("Only ADMIN, HRBP or TALENT_ACQUISITION can review rehire eligibility", exception.getMessage());
        verify(eligibilityRepository).findById(100L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(eligibilityRepository, userRepository);
    }

    @Test
    void updateEligibility_Success_AllFieldsProvided() {
        UpdateRehireEligibilityRequest request = new UpdateRehireEligibilityRequest();
        request.setReviewedById(3L);
        request.setEligibleForRehire(EligibleForRehire.NO);
        request.setCoolingOffPeriodMonths(12);
        request.setEligibilityReviewDate(LocalDate.of(2026, 6, 1));
        request.setEligibilityReason("New reason");
        request.setStatus(RehireEligibilityStatus.INELIGIBLE);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User oldReviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        User newReviewer = createUser(3L, "HRBP Reviewer", UserRole.HRBP);

        RehireEligibility existing = createEligibilityEntity(100L, alumni, oldReviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.NO)
                .coolingOffPeriodMonths(12)
                .eligibilityReviewDate(LocalDate.of(2026, 6, 1))
                .eligibilityReason("New reason")
                .reviewedBy(newReviewer)
                .status(RehireEligibilityStatus.INELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 3L, EligibleForRehire.NO, RehireEligibilityStatus.INELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newReviewer));
        when(eligibilityRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.updateEligibility(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(newReviewer, captured.getReviewedBy());
        assertEquals(EligibleForRehire.NO, captured.getEligibleForRehire());
        assertEquals(12, captured.getCoolingOffPeriodMonths());
        assertEquals(LocalDate.of(2026, 6, 1), captured.getEligibilityReviewDate());
        assertEquals("New reason", captured.getEligibilityReason());
        assertEquals(RehireEligibilityStatus.INELIGIBLE, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your rehire eligibility has been updated: INELIGIBLE",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP3", "UPDATE_REHIRE_ELIGIBILITY", "REHIRE_ELIGIBILITY");
    }

    @Test
    void updateEligibility_Success_FieldsNull_AndNoReviewerOnSavedEntity() {
        UpdateRehireEligibilityRequest request = new UpdateRehireEligibilityRequest();
        // all fields null

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);

        // existing has no reviewer (null) to test the branch in audit log: saved.getReviewedBy() != null ? saved.getReviewedBy().getUserId() : null
        RehireEligibility existing = createEligibilityEntity(100L, alumni, null, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);
        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, null, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(eligibilityRepository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.updateEligibility(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(eligibilityRepository).save(existing);
        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your rehire eligibility has been updated: ELIGIBLE",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        // verify audit log was called with null userId as reviewedBy is null on saved
        verify(auditLogService).record(null, "UPDATE_REHIRE_ELIGIBILITY", "REHIRE_ELIGIBILITY");
    }

    @Test
    void updateEligibility_Success_StatusResolvedFromEligibleForRehire() {
        UpdateRehireEligibilityRequest request = new UpdateRehireEligibilityRequest();
        request.setEligibleForRehire(EligibleForRehire.CONDITIONAL);
        // status is null, so it should resolve from EligibleForRehire

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);

        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(eligibilityRepository.save(existing)).thenReturn(existing);

        service.updateEligibility(100L, request);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(EligibleForRehire.CONDITIONAL, captured.getEligibleForRehire());
        assertEquals(RehireEligibilityStatus.UNDER_REVIEW, captured.getStatus()); // resolved status
    }

    // --- markEligible Tests ---

    @Test
    void markEligible_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.NO, RehireEligibilityStatus.INELIGIBLE);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.YES)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.ELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(eligibilityRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.markEligible(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(EligibleForRehire.YES, captured.getEligibleForRehire());
        assertEquals(RehireEligibilityStatus.ELIGIBLE, captured.getStatus());
        assertEquals(LocalDate.now(), captured.getEligibilityReviewDate());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "You are eligible for rehire.",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "MARK_REHIRE_ELIGIBLE", "REHIRE_ELIGIBILITY");
    }

    // --- markIneligible Tests ---

    @Test
    void markIneligible_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.NO)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.INELIGIBLE)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.NO, RehireEligibilityStatus.INELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(eligibilityRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.markIneligible(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(EligibleForRehire.NO, captured.getEligibleForRehire());
        assertEquals(RehireEligibilityStatus.INELIGIBLE, captured.getStatus());
        assertEquals(LocalDate.now(), captured.getEligibilityReviewDate());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your profile is currently not eligible for rehire.",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "MARK_REHIRE_INELIGIBLE", "REHIRE_ELIGIBILITY");
    }

    // --- markUnderReview Tests ---

    @Test
    void markUnderReview_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        RehireEligibility expectedSaved = RehireEligibility.builder()
                .eligibilityId(100L)
                .alumni(alumni)
                .eligibleForRehire(EligibleForRehire.CONDITIONAL)
                .coolingOffPeriodMonths(6)
                .eligibilityReviewDate(LocalDate.now())
                .reviewedBy(reviewer)
                .status(RehireEligibilityStatus.UNDER_REVIEW)
                .build();

        RehireEligibilityResponse expectedResponse = createEligibilityResponse(100L, 1L, 2L, EligibleForRehire.CONDITIONAL, RehireEligibilityStatus.UNDER_REVIEW);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(eligibilityRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        RehireEligibilityResponse response = service.markUnderReview(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<RehireEligibility> entityCaptor = ArgumentCaptor.forClass(RehireEligibility.class);
        verify(eligibilityRepository).save(entityCaptor.capture());
        RehireEligibility captured = entityCaptor.getValue();
        assertEquals(EligibleForRehire.CONDITIONAL, captured.getEligibleForRehire());
        assertEquals(RehireEligibilityStatus.UNDER_REVIEW, captured.getStatus());
        assertEquals(LocalDate.now(), captured.getEligibilityReviewDate());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your rehire eligibility is under review.",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "MARK_REHIRE_UNDER_REVIEW", "REHIRE_ELIGIBILITY");
    }

    // --- deleteEligibility Tests ---

    @Test
    void deleteEligibility_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User reviewer = createUser(2L, "Admin Reviewer", UserRole.ADMIN);
        RehireEligibility existing = createEligibilityEntity(100L, alumni, reviewer, EligibleForRehire.YES, RehireEligibilityStatus.ELIGIBLE);

        when(eligibilityRepository.findById(100L)).thenReturn(Optional.of(existing));
        doNothing().when(eligibilityRepository).delete(existing);

        service.deleteEligibility(100L);

        verify(eligibilityRepository).findById(100L);
        verify(eligibilityRepository).delete(existing);
    }
}
