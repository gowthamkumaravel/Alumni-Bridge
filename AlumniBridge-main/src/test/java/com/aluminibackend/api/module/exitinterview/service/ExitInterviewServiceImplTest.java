package com.aluminibackend.api.module.exitinterview.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.exitinterview.dto.request.CreateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.request.UpdateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.response.ExitInterviewResponse;
import com.aluminibackend.api.module.exitinterview.entity.ExitInterview;
import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import com.aluminibackend.api.module.exitinterview.enums.InterviewFormat;
import com.aluminibackend.api.module.exitinterview.mapper.ExitInterviewMapper;
import com.aluminibackend.api.module.exitinterview.repository.ExitInterviewRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
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
public class ExitInterviewServiceImplTest {

    @Mock
    private ExitInterviewRepository interviewRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExitInterviewMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ExitInterviewServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name, UserRole role) {
        return User.builder()
                .userId(userId)
                .name(name)
                .role(role)
                .email(name.toLowerCase().replace(" ", "") + "@example.com")
                .employeeId("EMP" + userId)
                .password("password")
                .accountType(AccountType.ACTIVE)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private OffboardingCase createOffboardingCase(Long caseId, User employee, User hrbpOwner) {
        return OffboardingCase.builder()
                .caseId(caseId)
                .employee(employee)
                .departureType(DepartureType.RESIGNATION)
                .lastWorkingDate(LocalDate.now())
                .hrbpOwner(hrbpOwner)
                .status(OffboardingStatus.IN_PROGRESS)
                .build();
    }

    private ExitInterview createExitInterview(Long interviewId, OffboardingCase offboardingCase, User employee, User interviewer, LocalDateTime scheduledDate, InterviewFormat format, ExitInterviewStatus status) {
        return ExitInterview.builder()
                .interviewId(interviewId)
                .offboardingCase(offboardingCase)
                .employee(employee)
                .interviewer(interviewer)
                .scheduledDate(scheduledDate)
                .format(format)
                .status(status)
                .build();
    }

    private CreateExitInterviewRequest createCreateRequest(Long caseId, Long interviewerId, LocalDateTime scheduledDate, InterviewFormat format) {
        CreateExitInterviewRequest request = new CreateExitInterviewRequest();
        request.setCaseId(caseId);
        request.setInterviewerId(interviewerId == null ? null : "EMP" + interviewerId);
        request.setScheduledDate(scheduledDate);
        request.setFormat(format);
        return request;
    }

    private UpdateExitInterviewRequest createUpdateRequest(Long interviewerId, LocalDateTime scheduledDate, InterviewFormat format, String primaryReason, String feedback, ExitInterviewStatus status) {
        UpdateExitInterviewRequest request = new UpdateExitInterviewRequest();
        request.setInterviewerId(interviewerId == null ? null : "EMP" + interviewerId);
        request.setScheduledDate(scheduledDate);
        request.setFormat(format);
        request.setPrimaryReasonForLeaving(primaryReason);
        request.setKeyFeedback(feedback);
        request.setStatus(status);
        return request;
    }

    private ExitInterviewResponse createResponse(Long interviewId, Long caseId, Long employeeId, String employeeName, Long interviewerId, String interviewerName, LocalDateTime scheduledDate, InterviewFormat format, String primaryReason, String feedback, ExitInterviewStatus status) {
        return ExitInterviewResponse.builder()
                .interviewId(interviewId)
                .caseId(caseId)
                .employeeId(employeeId == null ? null : "EMP" + employeeId)
                .employeeName(employeeName)
                .interviewerId(interviewerId == null ? null : "EMP" + interviewerId)
                .interviewerName(interviewerName)
                .scheduledDate(scheduledDate)
                .format(format)
                .primaryReasonForLeaving(primaryReason)
                .keyFeedback(feedback)
                .status(status)
                .build();
    }

    // --- scheduleInterview Tests ---

    @Test
    void scheduleInterview_Success_conductedByHrbp() {
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(5);
        CreateExitInterviewRequest request = createCreateRequest(1L, 2L, scheduledDate, InterviewFormat.VIRTUAL);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview savedInterview = createExitInterview(100L, offboardingCase, employee, hrbp, scheduledDate, InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", scheduledDate, InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findByEmployeeId("EMP2")).thenReturn(Optional.of(hrbp));
        when(interviewRepository.save(any(ExitInterview.class))).thenReturn(savedInterview);
        when(mapper.toResponse(savedInterview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.scheduleInterview(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<ExitInterview> captor = ArgumentCaptor.forClass(ExitInterview.class);
        verify(interviewRepository).save(captor.capture());
        ExitInterview captured = captor.getValue();
        assertEquals(offboardingCase, captured.getOffboardingCase());
        assertEquals(employee, captured.getEmployee());
        assertEquals(hrbp, captured.getInterviewer());
        assertEquals(scheduledDate, captured.getScheduledDate());
        assertEquals(InterviewFormat.VIRTUAL, captured.getFormat());
        assertEquals(ExitInterviewStatus.SCHEDULED, captured.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your exit interview has been scheduled.",
                NotificationCategory.EXIT_INTERVIEW
        );
        verify(auditLogService).record("EMP2", "SCHEDULE_EXIT_INTERVIEW", "EXIT_INTERVIEW");
        verify(interviewRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(mapper).toResponse(savedInterview);

        verifyNoMoreInteractions(interviewRepository, offboardingCaseRepository, userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void scheduleInterview_Success_conductedByAdmin() {
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(5);
        CreateExitInterviewRequest request = createCreateRequest(1L, 3L, scheduledDate, InterviewFormat.IN_PERSON);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Owner", UserRole.HRBP);
        User admin = createUser(3L, "Admin Interviewer", UserRole.ADMIN);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview savedInterview = createExitInterview(100L, offboardingCase, employee, admin, scheduledDate, InterviewFormat.IN_PERSON, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 3L, "Admin Interviewer", scheduledDate, InterviewFormat.IN_PERSON, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findByEmployeeId("EMP3")).thenReturn(Optional.of(admin));
        when(interviewRepository.save(any(ExitInterview.class))).thenReturn(savedInterview);
        when(mapper.toResponse(savedInterview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.scheduleInterview(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(notificationService).createSystemNotification("EMP10", "Your exit interview has been scheduled.", NotificationCategory.EXIT_INTERVIEW);
        verify(auditLogService).record("EMP3", "SCHEDULE_EXIT_INTERVIEW", "EXIT_INTERVIEW");
    }

    @Test
    void scheduleInterview_ThrowsException_WhenInterviewAlreadyExists() {
        CreateExitInterviewRequest request = createCreateRequest(1L, 2L, LocalDateTime.now(), InterviewFormat.VIRTUAL);

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.scheduleInterview(request);
        });

        assertEquals("Exit interview already scheduled for this case", exception.getMessage());
        verify(interviewRepository).existsByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(offboardingCaseRepository, userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void scheduleInterview_ThrowsException_WhenCaseNotFound() {
        CreateExitInterviewRequest request = createCreateRequest(1L, 2L, LocalDateTime.now(), InterviewFormat.VIRTUAL);

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.scheduleInterview(request);
        });

        assertEquals("Offboarding case not found", exception.getMessage());
        verify(interviewRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verifyNoMoreInteractions(interviewRepository, offboardingCaseRepository);
        verifyNoInteractions(userRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void scheduleInterview_ThrowsException_WhenInterviewerNotFound() {
        CreateExitInterviewRequest request = createCreateRequest(1L, 2L, LocalDateTime.now(), InterviewFormat.VIRTUAL);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(9L, "HRBP Owner", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findByEmployeeId("EMP2")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.scheduleInterview(request);
        });

        assertEquals("Interviewer not found", exception.getMessage());
        verify(interviewRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(interviewRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    @Test
    void scheduleInterview_ThrowsException_WhenInterviewerHasInvalidRole() {
        CreateExitInterviewRequest request = createCreateRequest(1L, 2L, LocalDateTime.now(), InterviewFormat.VIRTUAL);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(9L, "HRBP Owner", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        User interviewer = createUser(2L, "Normal Employee", UserRole.EMPLOYEE); // Not HRBP or ADMIN

        when(interviewRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findByEmployeeId("EMP2")).thenReturn(Optional.of(interviewer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.scheduleInterview(request);
        });

        assertEquals("Only HRBP or ADMIN can conduct exit interview", exception.getMessage());
        verify(interviewRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(interviewRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, notificationService, auditLogService);
    }

    // --- getInterviewById Tests ---

    @Test
    void getInterviewById_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(interview));
        when(mapper.toResponse(interview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.getInterviewById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(interviewRepository).findById(100L);
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getInterviewById_ThrowsException_WhenNotFound() {
        when(interviewRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getInterviewById(100L);
        });

        assertEquals("Exit interview not found with ID: 100", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- getInterviewByCase Tests ---

    @Test
    void getInterviewByCase_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.of(interview));
        when(mapper.toResponse(interview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.getInterviewByCase(1L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(interviewRepository).findByOffboardingCase_CaseId(1L);
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getInterviewByCase_ThrowsException_WhenNotFound() {
        when(interviewRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getInterviewByCase(1L);
        });

        assertEquals("Exit interview not found for case ID: 1", exception.getMessage());
        verify(interviewRepository).findByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- getAllInterviews Tests ---

    @Test
    void getAllInterviews_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse responseDto = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findAll()).thenReturn(Collections.singletonList(interview));
        when(mapper.toResponse(interview)).thenReturn(responseDto);

        List<ExitInterviewResponse> results = service.getAllInterviews();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(interviewRepository).findAll();
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getAllInterviews_Empty() {
        when(interviewRepository.findAll()).thenReturn(Collections.emptyList());

        List<ExitInterviewResponse> results = service.getAllInterviews();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(interviewRepository).findAll();
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- getInterviewsByEmployee Tests ---

    @Test
    void getInterviewsByEmployee_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse responseDto = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findByEmployee_EmployeeId("EMP10")).thenReturn(Collections.singletonList(interview));
        when(mapper.toResponse(interview)).thenReturn(responseDto);

        List<ExitInterviewResponse> results = service.getInterviewsByEmployee("EMP10");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(interviewRepository).findByEmployee_EmployeeId("EMP10");
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getInterviewsByEmployee_Empty() {
        when(interviewRepository.findByEmployee_EmployeeId("EMP10")).thenReturn(Collections.emptyList());

        List<ExitInterviewResponse> results = service.getInterviewsByEmployee("EMP10");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(interviewRepository).findByEmployee_EmployeeId("EMP10");
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- getInterviewsByInterviewer Tests ---

    @Test
    void getInterviewsByInterviewer_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse responseDto = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findByInterviewer_EmployeeId("EMP2")).thenReturn(Collections.singletonList(interview));
        when(mapper.toResponse(interview)).thenReturn(responseDto);

        List<ExitInterviewResponse> results = service.getInterviewsByInterviewer("EMP2");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(interviewRepository).findByInterviewer_EmployeeId("EMP2");
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getInterviewsByInterviewer_Empty() {
        when(interviewRepository.findByInterviewer_EmployeeId("EMP2")).thenReturn(Collections.emptyList());

        List<ExitInterviewResponse> results = service.getInterviewsByInterviewer("EMP2");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(interviewRepository).findByInterviewer_EmployeeId("EMP2");
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- getInterviewsByStatus Tests ---

    @Test
    void getInterviewsByStatus_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitInterview interview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse responseDto = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", interview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findByStatus(ExitInterviewStatus.SCHEDULED)).thenReturn(Collections.singletonList(interview));
        when(mapper.toResponse(interview)).thenReturn(responseDto);

        List<ExitInterviewResponse> results = service.getInterviewsByStatus(ExitInterviewStatus.SCHEDULED);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(interviewRepository).findByStatus(ExitInterviewStatus.SCHEDULED);
        verify(mapper).toResponse(interview);
        verifyNoMoreInteractions(interviewRepository, mapper);
    }

    @Test
    void getInterviewsByStatus_Empty() {
        when(interviewRepository.findByStatus(ExitInterviewStatus.SCHEDULED)).thenReturn(Collections.emptyList());

        List<ExitInterviewResponse> results = service.getInterviewsByStatus(ExitInterviewStatus.SCHEDULED);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(interviewRepository).findByStatus(ExitInterviewStatus.SCHEDULED);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(mapper);
    }

    // --- updateInterview Tests ---

    @Test
    void updateInterview_Success_AllFieldsChanged() {
        LocalDateTime newScheduled = LocalDateTime.now().plusDays(10);
        UpdateExitInterviewRequest request = createUpdateRequest(3L, newScheduled, InterviewFormat.IN_PERSON, "Higher compensation", "Better feedback", ExitInterviewStatus.DECLINED);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User oldHrbp = createUser(2L, "Old HRBP", UserRole.HRBP);
        User newHrbp = createUser(3L, "New HRBP", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, oldHrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, oldHrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterview updatedInterview = createExitInterview(100L, offboardingCase, employee, newHrbp, newScheduled, InterviewFormat.IN_PERSON, ExitInterviewStatus.DECLINED);
        updatedInterview.setPrimaryReasonForLeaving("Higher compensation");
        updatedInterview.setKeyFeedback("Better feedback");

        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 3L, "New HRBP", newScheduled, InterviewFormat.IN_PERSON, "Higher compensation", "Better feedback", ExitInterviewStatus.DECLINED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        when(userRepository.findByEmployeeId("EMP3")).thenReturn(Optional.of(newHrbp));
        when(interviewRepository.save(existingInterview)).thenReturn(updatedInterview);
        when(mapper.toResponse(updatedInterview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.updateInterview(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(newHrbp, existingInterview.getInterviewer());
        assertEquals(newScheduled, existingInterview.getScheduledDate());
        assertEquals(InterviewFormat.IN_PERSON, existingInterview.getFormat());
        assertEquals("Higher compensation", existingInterview.getPrimaryReasonForLeaving());
        assertEquals("Better feedback", existingInterview.getKeyFeedback());
        assertEquals(ExitInterviewStatus.DECLINED, existingInterview.getStatus());

        verify(interviewRepository).findById(100L);
        verify(userRepository).findById(3L);
        verify(interviewRepository).save(existingInterview);
        verify(auditLogService).record("EMP3", "UPDATE_EXIT_INTERVIEW", "EXIT_INTERVIEW");
        verify(mapper).toResponse(updatedInterview);
        verifyNoMoreInteractions(interviewRepository, userRepository, mapper, auditLogService);
    }

    @Test
    void updateInterview_Success_NullFields() {
        UpdateExitInterviewRequest request = createUpdateRequest(null, null, null, null, null, null);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", existingInterview.getScheduledDate(), InterviewFormat.VIRTUAL, null, null, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        when(interviewRepository.save(existingInterview)).thenReturn(existingInterview);
        when(mapper.toResponse(existingInterview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.updateInterview(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(interviewRepository).findById(100L);
        verify(interviewRepository).save(existingInterview);
        verify(auditLogService).record("EMP2", "UPDATE_EXIT_INTERVIEW", "EXIT_INTERVIEW");
        verify(mapper).toResponse(existingInterview);
        verifyNoMoreInteractions(interviewRepository, mapper, auditLogService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateInterview_ThrowsException_WhenInterviewNotFound() {
        UpdateExitInterviewRequest request = createUpdateRequest(3L, null, null, null, null, null);

        when(interviewRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateInterview(100L, request);
        });

        assertEquals("Exit interview not found with ID: 100", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(userRepository, mapper, auditLogService);
    }

    @Test
    void updateInterview_ThrowsException_WhenInterviewerNotFound() {
        UpdateExitInterviewRequest request = createUpdateRequest(3L, null, null, null, null, null);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        when(userRepository.findByEmployeeId("EMP3")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateInterview(100L, request);
        });

        assertEquals("Interviewer not found", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verify(userRepository).findById(3L);
        verifyNoMoreInteractions(interviewRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    @Test
    void updateInterview_ThrowsException_WhenInterviewerHasInvalidRole() {
        UpdateExitInterviewRequest request = createUpdateRequest(3L, null, null, null, null, null);

        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        User invalidInterviewer = createUser(3L, "Employee Role", UserRole.EMPLOYEE); // Not HRBP or ADMIN

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        when(userRepository.findByEmployeeId("EMP3")).thenReturn(Optional.of(invalidInterviewer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateInterview(100L, request);
        });

        assertEquals("Only HRBP or ADMIN can conduct exit interview", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verify(userRepository).findById(3L);
        verifyNoMoreInteractions(interviewRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    // --- completeInterview Tests ---

    @Test
    void completeInterview_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);
        ExitInterview updatedInterview = createExitInterview(100L, offboardingCase, employee, hrbp, existingInterview.getScheduledDate(), InterviewFormat.VIRTUAL, ExitInterviewStatus.COMPLETED);
        updatedInterview.setPrimaryReasonForLeaving("Health Issues");
        updatedInterview.setKeyFeedback("Great culture");

        ExitInterviewResponse expectedResponse = createResponse(100L, 1L, 10L, "Employee User", 2L, "HRBP Interviewer", existingInterview.getScheduledDate(), InterviewFormat.VIRTUAL, "Health Issues", "Great culture", ExitInterviewStatus.COMPLETED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        when(interviewRepository.save(existingInterview)).thenReturn(updatedInterview);
        when(mapper.toResponse(updatedInterview)).thenReturn(expectedResponse);

        ExitInterviewResponse response = service.completeInterview(100L, "Health Issues", "Great culture");

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals("Health Issues", existingInterview.getPrimaryReasonForLeaving());
        assertEquals("Great culture", existingInterview.getKeyFeedback());
        assertEquals(ExitInterviewStatus.COMPLETED, existingInterview.getStatus());

        verify(interviewRepository).findById(100L);
        verify(interviewRepository).save(existingInterview);
        verify(notificationService).createSystemNotification(
                "EMP10",
                "Your exit interview has been marked completed.",
                NotificationCategory.EXIT_INTERVIEW
        );
        verify(auditLogService).record("EMP2", "COMPLETE_EXIT_INTERVIEW", "EXIT_INTERVIEW");
        verify(mapper).toResponse(updatedInterview);
        verifyNoMoreInteractions(interviewRepository, mapper, notificationService, auditLogService);
    }

    @Test
    void completeInterview_ThrowsException_WhenInterviewNotFound() {
        when(interviewRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.completeInterview(100L, "Reason", "Feedback");
        });

        assertEquals("Exit interview not found with ID: 100", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verifyNoMoreInteractions(interviewRepository);
        verifyNoInteractions(notificationService, auditLogService, mapper);
    }

    // --- deleteInterview Tests ---

    @Test
    void deleteInterview_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP Interviewer", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitInterview existingInterview = createExitInterview(100L, offboardingCase, employee, hrbp, LocalDateTime.now(), InterviewFormat.VIRTUAL, ExitInterviewStatus.SCHEDULED);

        when(interviewRepository.findById(100L)).thenReturn(Optional.of(existingInterview));
        doNothing().when(interviewRepository).delete(existingInterview);

        service.deleteInterview(100L);

        verify(interviewRepository).findById(100L);
        verify(interviewRepository).delete(existingInterview);
        verifyNoMoreInteractions(interviewRepository);
    }

    @Test
    void deleteInterview_ThrowsException_WhenInterviewNotFound() {
        when(interviewRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteInterview(100L);
        });

        assertEquals("Exit interview not found with ID: 100", exception.getMessage());
        verify(interviewRepository).findById(100L);
        verifyNoMoreInteractions(interviewRepository);
    }
}
