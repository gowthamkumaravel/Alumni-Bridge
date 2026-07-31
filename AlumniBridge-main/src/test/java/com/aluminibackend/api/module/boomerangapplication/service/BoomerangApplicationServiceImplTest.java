package com.aluminibackend.api.module.boomerangapplication.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.boomerangapplication.dto.request.CreateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.request.UpdateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.response.BoomerangApplicationResponse;
import com.aluminibackend.api.module.boomerangapplication.entity.BoomerangApplication;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import com.aluminibackend.api.module.boomerangapplication.mapper.BoomerangApplicationMapper;
import com.aluminibackend.api.module.boomerangapplication.repository.BoomerangApplicationRepository;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BoomerangApplicationServiceImplTest {

    @Mock
    private BoomerangApplicationRepository applicationRepository;

    @Mock
    private AlumniProfileRepository alumniProfileRepository;

    @Mock
    private RehireEligibilityRepository rehireEligibilityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoomerangApplicationMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private BoomerangApplicationServiceImpl service;

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

    private RehireEligibility createEligibilityEntity(Long id, AlumniProfile alumni, RehireEligibilityStatus status) {
        return RehireEligibility.builder()
                .eligibilityId(id)
                .alumni(alumni)
                .status(status)
                .build();
    }

    private BoomerangApplication createApplicationEntity(Long id, AlumniProfile alumni, String jobReqId, Double tenure, User recruiter, BoomerangApplicationStatus status) {
        return BoomerangApplication.builder()
                .applicationId(id)
                .alumni(alumni)
                .jobRequisitionId(jobReqId)
                .previousTenureYears(tenure)
                .recruiter(recruiter)
                .status(status)
                .applicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private BoomerangApplicationResponse createApplicationResponse(Long id, Long alumniId, String jobReqId, Double tenure, Long recruiterId, BoomerangApplicationStatus status) {
        return BoomerangApplicationResponse.builder()
                .applicationId(id)
                .alumniId(alumniId)
                .alumniName("Alumni " + alumniId)
                .formerEmployeeId("FORMER_" + alumniId)
                .jobRequisitionId(jobReqId)
                .applicationDate(LocalDate.now())
                .previousTenureYears(tenure)
                .recruiterId(recruiterId)
                .recruiterName("Recruiter")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- createApplication Tests ---

    @Test
    void createApplication_ThrowsException_WhenApplicationAlreadyExists() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Application already exists for this alumni and job requisition", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verifyNoMoreInteractions(applicationRepository);
        verifyNoInteractions(alumniProfileRepository, rehireEligibilityRepository, userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_ThrowsException_WhenAlumniProfileNotFound() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Alumni profile not found", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verify(alumniProfileRepository).findById(1L);
        verifyNoMoreInteractions(applicationRepository, alumniProfileRepository);
        verifyNoInteractions(rehireEligibilityRepository, userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_ThrowsException_WhenRehireEligibilityNotFound() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Rehire eligibility not found for this alumni", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verify(alumniProfileRepository).findById(1L);
        verify(rehireEligibilityRepository).findByAlumni_AlumniId(1L);
        verifyNoMoreInteractions(applicationRepository, alumniProfileRepository, rehireEligibilityRepository);
        verifyNoInteractions(userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_ThrowsException_WhenAlumniNotEligible() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.INELIGIBLE);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Alumni is not eligible for rehire. Current status: INELIGIBLE", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verify(alumniProfileRepository).findById(1L);
        verify(rehireEligibilityRepository).findByAlumni_AlumniId(1L);
        verifyNoMoreInteractions(applicationRepository, alumniProfileRepository, rehireEligibilityRepository);
        verifyNoInteractions(userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_ThrowsException_WhenRecruiterNotFound() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");
        request.setRecruiterId(2L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.ELIGIBLE);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Recruiter not found", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verify(alumniProfileRepository).findById(1L);
        verify(rehireEligibilityRepository).findByAlumni_AlumniId(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(applicationRepository, alumniProfileRepository, rehireEligibilityRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_ThrowsException_WhenRecruiterRoleInvalid() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");
        request.setRecruiterId(2L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.ELIGIBLE);
        User invalidRecruiter = createUser(2L, "Employee Recruiter", UserRole.EMPLOYEE);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invalidRecruiter));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createApplication(request);
        });

        assertEquals("Only ADMIN, TALENT_ACQUISITION or HRBP can manage boomerang applications", exception.getMessage());
        verify(applicationRepository).existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123");
        verify(alumniProfileRepository).findById(1L);
        verify(rehireEligibilityRepository).findByAlumni_AlumniId(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(applicationRepository, alumniProfileRepository, rehireEligibilityRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    @Test
    void createApplication_Success_RecruiterAdmin() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");
        request.setPreviousTenureYears(3.5);
        request.setRecruiterId(2L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.ELIGIBLE);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);

        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(applicationRepository.save(any(BoomerangApplication.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.createApplication(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<BoomerangApplication> applicationCaptor = ArgumentCaptor.forClass(BoomerangApplication.class);
        verify(applicationRepository).save(applicationCaptor.capture());
        BoomerangApplication captured = applicationCaptor.getValue();
        assertEquals(alumni, captured.getAlumni());
        assertEquals("JOB-123", captured.getJobRequisitionId());
        assertEquals(3.5, captured.getPreviousTenureYears());
        assertEquals(recruiter, captured.getRecruiter());
        assertEquals(BoomerangApplicationStatus.APPLIED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your boomerang application has been submitted for job JOB-123",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "CREATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");
        verify(mapper).toResponse(expectedSaved);
    }

    @Test
    void createApplication_Success_RecruiterTalentAcquisition() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");
        request.setPreviousTenureYears(2.0);
        request.setRecruiterId(3L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.ELIGIBLE);
        User recruiter = createUser(3L, "TA Recruiter", UserRole.TALENT_ACQUISITION);

        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-123", 2.0, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 2.0, 3L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));
        when(userRepository.findById(3L)).thenReturn(Optional.of(recruiter));
        when(applicationRepository.save(any(BoomerangApplication.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.createApplication(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(auditLogService).record("EMP3", "CREATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");
    }

    @Test
    void createApplication_Success_RecruiterHrbp() {
        CreateBoomerangApplicationRequest request = new CreateBoomerangApplicationRequest();
        request.setAlumniId(1L);
        request.setJobRequisitionId("JOB-123");
        request.setPreviousTenureYears(1.0);
        request.setRecruiterId(4L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        RehireEligibility eligibility = createEligibilityEntity(100L, alumni, RehireEligibilityStatus.ELIGIBLE);
        User recruiter = createUser(4L, "HRBP Recruiter", UserRole.HRBP);

        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-123", 1.0, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 1.0, 4L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(1L, "JOB-123")).thenReturn(false);
        when(alumniProfileRepository.findById(1L)).thenReturn(Optional.of(alumni));
        when(rehireEligibilityRepository.findByAlumni_AlumniId(1L)).thenReturn(Optional.of(eligibility));
        when(userRepository.findById(4L)).thenReturn(Optional.of(recruiter));
        when(applicationRepository.save(any(BoomerangApplication.class))).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.createApplication(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(auditLogService).record("EMP4", "CREATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");
    }

    // --- getApplicationById Tests ---

    @Test
    void getApplicationById_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.getApplicationById(50L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(applicationRepository).findById(50L);
        verify(mapper).toResponse(application);
    }

    @Test
    void getApplicationById_ThrowsException_WhenNotFound() {
        when(applicationRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getApplicationById(50L);
        });

        assertEquals("Boomerang application not found with ID: 50", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verifyNoInteractions(mapper);
    }

    // --- getAllApplications Tests ---

    @Test
    void getAllApplications_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findAll()).thenReturn(Collections.singletonList(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        List<BoomerangApplicationResponse> responses = service.getAllApplications();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(applicationRepository).findAll();
        verify(mapper).toResponse(application);
    }

    @Test
    void getAllApplications_Success_Empty() {
        when(applicationRepository.findAll()).thenReturn(Collections.emptyList());

        List<BoomerangApplicationResponse> responses = service.getAllApplications();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(applicationRepository).findAll();
        verifyNoInteractions(mapper);
    }

    // --- getApplicationsByAlumni Tests ---

    @Test
    void getApplicationsByAlumni_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findByAlumni_AlumniId(1L)).thenReturn(Collections.singletonList(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        List<BoomerangApplicationResponse> responses = service.getApplicationsByAlumni(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(applicationRepository).findByAlumni_AlumniId(1L);
    }

    // --- getApplicationsByRecruiter Tests ---

    @Test
    void getApplicationsByRecruiter_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findByRecruiter_UserId(2L)).thenReturn(Collections.singletonList(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        List<BoomerangApplicationResponse> responses = service.getApplicationsByRecruiter(2L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(applicationRepository).findByRecruiter_UserId(2L);
    }

    // --- getApplicationsByJob Tests ---

    @Test
    void getApplicationsByJob_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findByJobRequisitionId("JOB-123")).thenReturn(Collections.singletonList(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        List<BoomerangApplicationResponse> responses = service.getApplicationsByJob("JOB-123");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(applicationRepository).findByJobRequisitionId("JOB-123");
    }

    // --- getApplicationsByStatus Tests ---

    @Test
    void getApplicationsByStatus_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.SHORTLISTED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.SHORTLISTED);

        when(applicationRepository.findByStatus(BoomerangApplicationStatus.SHORTLISTED)).thenReturn(Collections.singletonList(application));
        when(mapper.toResponse(application)).thenReturn(expectedResponse);

        List<BoomerangApplicationResponse> responses = service.getApplicationsByStatus(BoomerangApplicationStatus.SHORTLISTED);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedResponse, responses.get(0));
        verify(applicationRepository).findByStatus(BoomerangApplicationStatus.SHORTLISTED);
    }

    // --- updateApplication Tests ---

    @Test
    void updateApplication_ThrowsException_WhenNotFound() {
        UpdateBoomerangApplicationRequest request = new UpdateBoomerangApplicationRequest();
        when(applicationRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateApplication(50L, request);
        });

        assertEquals("Boomerang application not found with ID: 50", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void updateApplication_ThrowsException_WhenRecruiterNotFound() {
        UpdateBoomerangApplicationRequest request = new UpdateBoomerangApplicationRequest();
        request.setRecruiterId(99L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(application));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateApplication(50L, request);
        });

        assertEquals("Recruiter not found", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(applicationRepository, userRepository);
    }

    @Test
    void updateApplication_ThrowsException_WhenRecruiterRoleInvalid() {
        UpdateBoomerangApplicationRequest request = new UpdateBoomerangApplicationRequest();
        request.setRecruiterId(99L);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication application = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        User invalidRecruiter = createUser(99L, "Employee Recruiter", UserRole.EMPLOYEE);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(application));
        when(userRepository.findById(99L)).thenReturn(Optional.of(invalidRecruiter));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateApplication(50L, request);
        });

        assertEquals("Only ADMIN, TALENT_ACQUISITION or HRBP can manage boomerang applications", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(applicationRepository, userRepository);
    }

    @Test
    void updateApplication_Success_UpdateAllFields() {
        UpdateBoomerangApplicationRequest request = new UpdateBoomerangApplicationRequest();
        request.setJobRequisitionId("JOB-NEW");
        request.setPreviousTenureYears(5.0);
        request.setRecruiterId(3L);
        request.setStatus(BoomerangApplicationStatus.SHORTLISTED);

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User oldRecruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        User newRecruiter = createUser(3L, "TA Recruiter", UserRole.TALENT_ACQUISITION);

        BoomerangApplication existing = createApplicationEntity(50L, alumni, "JOB-123", 3.5, oldRecruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-NEW", 5.0, newRecruiter, BoomerangApplicationStatus.SHORTLISTED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-NEW", 5.0, 3L, BoomerangApplicationStatus.SHORTLISTED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newRecruiter));
        when(applicationRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.updateApplication(50L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<BoomerangApplication> applicationCaptor = ArgumentCaptor.forClass(BoomerangApplication.class);
        verify(applicationRepository).save(applicationCaptor.capture());
        BoomerangApplication captured = applicationCaptor.getValue();
        assertEquals("JOB-NEW", captured.getJobRequisitionId());
        assertEquals(5.0, captured.getPreviousTenureYears());
        assertEquals(newRecruiter, captured.getRecruiter());
        assertEquals(BoomerangApplicationStatus.SHORTLISTED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your boomerang application has been updated: SHORTLISTED",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP3", "UPDATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");
    }

    @Test
    void updateApplication_Success_FieldsNull_AndNoRecruiterOnSavedEntity() {
        UpdateBoomerangApplicationRequest request = new UpdateBoomerangApplicationRequest();
        // All fields are null, so none should be modified

        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);

        // We pass null for recruiter to test the branch: saved.getRecruiter() != null ? saved.getRecruiter().getUserId() : null
        BoomerangApplication existing = createApplicationEntity(50L, alumni, "JOB-123", 3.5, null, BoomerangApplicationStatus.APPLIED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, null, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.updateApplication(50L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(applicationRepository).save(existing);
        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your boomerang application has been updated: APPLIED",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        // Verify audit log has null userId since recruiter is null on the saved entity
        verify(auditLogService).record(null, "UPDATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");
    }

    // --- updateStatus Tests ---

    @Test
    void updateStatus_ThrowsException_WhenNotFound() {
        when(applicationRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateStatus(50L, BoomerangApplicationStatus.INTERVIEWED);
        });

        assertEquals("Boomerang application not found with ID: 50", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void updateStatus_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication existing = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);
        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.INTERVIEWED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, 2L, BoomerangApplicationStatus.INTERVIEWED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.updateStatus(50L, BoomerangApplicationStatus.INTERVIEWED);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<BoomerangApplication> applicationCaptor = ArgumentCaptor.forClass(BoomerangApplication.class);
        verify(applicationRepository).save(applicationCaptor.capture());
        assertEquals(BoomerangApplicationStatus.INTERVIEWED, applicationCaptor.getValue().getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your boomerang application status is now: INTERVIEWED",
                NotificationCategory.REHIRE_OPPORTUNITY
        );
        verify(auditLogService).record("EMP2", "UPDATE_BOOMERANG_STATUS", "BOOMERANG_APPLICATION");
    }

    @Test
    void updateStatus_Success_NoRecruiterOnSavedEntity() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        BoomerangApplication existing = createApplicationEntity(50L, alumni, "JOB-123", 3.5, null, BoomerangApplicationStatus.APPLIED);
        BoomerangApplication expectedSaved = createApplicationEntity(50L, alumni, "JOB-123", 3.5, null, BoomerangApplicationStatus.INTERVIEWED);
        BoomerangApplicationResponse expectedResponse = createApplicationResponse(50L, 1L, "JOB-123", 3.5, null, BoomerangApplicationStatus.INTERVIEWED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(expectedSaved);
        when(mapper.toResponse(expectedSaved)).thenReturn(expectedResponse);

        BoomerangApplicationResponse response = service.updateStatus(50L, BoomerangApplicationStatus.INTERVIEWED);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(auditLogService).record(null, "UPDATE_BOOMERANG_STATUS", "BOOMERANG_APPLICATION");
    }

    // --- deleteApplication Tests ---

    @Test
    void deleteApplication_ThrowsException_WhenNotFound() {
        when(applicationRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteApplication(50L);
        });

        assertEquals("Boomerang application not found with ID: 50", exception.getMessage());
        verify(applicationRepository).findById(50L);
        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void deleteApplication_Success() {
        User alumniUser = createUser(10L, "Alumni User", UserRole.EMPLOYEE);
        AlumniProfile alumni = createAlumniProfile(1L, alumniUser);
        User recruiter = createUser(2L, "Admin Recruiter", UserRole.ADMIN);
        BoomerangApplication existing = createApplicationEntity(50L, alumni, "JOB-123", 3.5, recruiter, BoomerangApplicationStatus.APPLIED);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(existing));
        doNothing().when(applicationRepository).delete(existing);

        service.deleteApplication(50L);

        verify(applicationRepository).findById(50L);
        verify(applicationRepository).delete(existing);
    }
}
