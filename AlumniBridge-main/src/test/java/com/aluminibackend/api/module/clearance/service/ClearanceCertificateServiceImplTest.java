package com.aluminibackend.api.module.clearance.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.clearance.dto.request.CreateClearanceCertificateRequest;
import com.aluminibackend.api.module.clearance.dto.response.ClearanceCertificateResponse;
import com.aluminibackend.api.module.clearance.entity.ClearanceCertificate;
import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;
import com.aluminibackend.api.module.clearance.mapper.ClearanceCertificateMapper;
import com.aluminibackend.api.module.clearance.repository.ClearanceCertificateRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
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
public class ClearanceCertificateServiceImplTest {

    @Mock
    private ClearanceCertificateRepository certificateRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private OffboardingTaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClearanceCertificateMapper mapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ClearanceCertificateServiceImpl service;

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

    private OffboardingCase createOffboardingCase(Long caseId, User employee, OffboardingStatus status) {
        return OffboardingCase.builder()
                .caseId(caseId)
                .employee(employee)
                .departureType(DepartureType.RESIGNATION)
                .lastWorkingDate(LocalDate.now())
                .hrbpOwner(createUser(999L, "HR Owner", UserRole.HRBP))
                .status(status)
                .build();
    }

    private OffboardingTask createOffboardingTask(Long taskId, OffboardingCase offboardingCase, TaskCategory category, String description, TaskStatus status) {
        return OffboardingTask.builder()
                .taskId(taskId)
                .offboardingCase(offboardingCase)
                .category(category)
                .taskDescription(description)
                .assignedTo(createUser(888L, "Task Executor", UserRole.EMPLOYEE))
                .dueDate(LocalDate.now().plusDays(5))
                .status(status)
                .build();
    }

    private ClearanceCertificate createClearanceCertificate(Long certId, OffboardingCase offboardingCase, User issuedBy, ClearanceStatus status, String outstandingItems) {
        return ClearanceCertificate.builder()
                .certId(certId)
                .offboardingCase(offboardingCase)
                .issuedBy(issuedBy)
                .outstandingItems(outstandingItems)
                .status(status)
                .issuedDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateClearanceCertificateRequest createRequest(Long caseId, Long issuedById) {
        CreateClearanceCertificateRequest request = new CreateClearanceCertificateRequest();
        request.setCaseId(caseId);
        request.setIssuedById(issuedById);
        return request;
    }

    private ClearanceCertificateResponse createResponse(Long certId, Long caseId, Long issuedById, ClearanceStatus status, String outstandingItems) {
        return ClearanceCertificateResponse.builder()
                .certId(certId)
                .caseId(caseId)
                .employeeId(10L)
                .employeeName("Test Employee")
                .issuedDate(LocalDate.now())
                .issuedById(issuedById)
                .issuedByName("Issuer Name")
                .outstandingItems(outstandingItems)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- issueCertificate Tests ---

    @Test
    void issueCertificate_ThrowsException_WhenCertificateAlreadyExists() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.issueCertificate(request);
        });

        assertEquals("Clearance certificate already issued for this case", exception.getMessage());
        verify(certificateRepository).existsByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(offboardingCaseRepository, userRepository, taskRepository, mapper, auditLogService);
    }

    @Test
    void issueCertificate_ThrowsException_WhenOffboardingCaseNotFound() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.issueCertificate(request);
        });

        assertEquals("Offboarding case not found", exception.getMessage());
        verify(certificateRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verifyNoMoreInteractions(certificateRepository, offboardingCaseRepository);
        verifyNoInteractions(userRepository, taskRepository, mapper, auditLogService);
    }

    @Test
    void issueCertificate_ThrowsException_WhenIssuedByUserNotFound() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.IN_PROGRESS);

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.issueCertificate(request);
        });

        assertEquals("Issued by user not found", exception.getMessage());
        verify(certificateRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(certificateRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(taskRepository, mapper, auditLogService);
    }

    @Test
    void issueCertificate_ThrowsException_WhenIssuedByUserHasInvalidRole() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.IN_PROGRESS);
        User issuedBy = createUser(2L, "Employee Issuer", UserRole.EMPLOYEE); // Not ADMIN or HRBP

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(issuedBy));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.issueCertificate(request);
        });

        assertEquals("Only ADMIN or HRBP can issue clearance certificate", exception.getMessage());
        verify(certificateRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(certificateRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(taskRepository, mapper, auditLogService);
    }

    @Test
    void issueCertificate_Success_NoOutstandingTasks_AdminIssuer() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.IN_PROGRESS);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);

        // All tasks are completed or waived
        OffboardingTask task1 = createOffboardingTask(101L, offboardingCase, TaskCategory.IT, "Return Laptop", TaskStatus.COMPLETED);
        OffboardingTask task2 = createOffboardingTask(102L, offboardingCase, TaskCategory.HR, "Exit Interview", TaskStatus.WAIVED);
        List<OffboardingTask> tasks = Arrays.asList(task1, task2);

        ClearanceCertificate savedCertificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse expectedResponse = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(issuedBy));
        when(taskRepository.findByOffboardingCase_CaseId(1L)).thenReturn(tasks);
        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(offboardingCase);
        when(certificateRepository.save(any(ClearanceCertificate.class))).thenReturn(savedCertificate);
        when(mapper.toResponse(savedCertificate)).thenReturn(expectedResponse);

        ClearanceCertificateResponse response = service.issueCertificate(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        // Verify OffboardingCase status updated to COMPLETED
        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
        verify(offboardingCaseRepository).save(caseCaptor.capture());
        assertEquals(OffboardingStatus.COMPLETED, caseCaptor.getValue().getStatus());

        // Verify ClearanceCertificate details
        ArgumentCaptor<ClearanceCertificate> certCaptor = ArgumentCaptor.forClass(ClearanceCertificate.class);
        verify(certificateRepository).save(certCaptor.capture());
        ClearanceCertificate capturedCert = certCaptor.getValue();
        assertEquals(offboardingCase, capturedCert.getOffboardingCase());
        assertEquals(issuedBy, capturedCert.getIssuedBy());
        assertEquals(ClearanceStatus.CLEAR, capturedCert.getStatus());
        assertEquals("No outstanding items", capturedCert.getOutstandingItems());

        // Verify Audit interaction
        verify(auditLogService).record("EMP2", "ISSUE_CLEARANCE_CERTIFICATE", "CLEARANCE");

        verify(certificateRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(taskRepository).findByOffboardingCase_CaseId(1L);
        verify(mapper).toResponse(savedCertificate);

        verifyNoMoreInteractions(certificateRepository, offboardingCaseRepository, userRepository, taskRepository, mapper, auditLogService);
    }

    @Test
    void issueCertificate_Success_NoTasksAtAll_HrbpIssuer() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.IN_PROGRESS);
        User issuedBy = createUser(2L, "HRBP Issuer", UserRole.HRBP);

        List<OffboardingTask> tasks = Collections.emptyList();

        ClearanceCertificate savedCertificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse expectedResponse = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(issuedBy));
        when(taskRepository.findByOffboardingCase_CaseId(1L)).thenReturn(tasks);
        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(offboardingCase);
        when(certificateRepository.save(any(ClearanceCertificate.class))).thenReturn(savedCertificate);
        when(mapper.toResponse(savedCertificate)).thenReturn(expectedResponse);

        ClearanceCertificateResponse response = service.issueCertificate(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        // Verify OffboardingCase status updated to COMPLETED
        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
        verify(offboardingCaseRepository).save(caseCaptor.capture());
        assertEquals(OffboardingStatus.COMPLETED, caseCaptor.getValue().getStatus());

        // Verify ClearanceCertificate details
        ArgumentCaptor<ClearanceCertificate> certCaptor = ArgumentCaptor.forClass(ClearanceCertificate.class);
        verify(certificateRepository).save(certCaptor.capture());
        ClearanceCertificate capturedCert = certCaptor.getValue();
        assertEquals(ClearanceStatus.CLEAR, capturedCert.getStatus());
        assertEquals("No outstanding items", capturedCert.getOutstandingItems());

        verify(auditLogService).record("EMP2", "ISSUE_CLEARANCE_CERTIFICATE", "CLEARANCE");
    }

    @Test
    void issueCertificate_Success_WithOutstandingTasks() {
        CreateClearanceCertificateRequest request = createRequest(1L, 2L);
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.IN_PROGRESS);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);

        // Outstanding tasks: pending & in progress
        OffboardingTask task1 = createOffboardingTask(101L, offboardingCase, TaskCategory.IT, "Return Laptop", TaskStatus.PENDING);
        OffboardingTask task2 = createOffboardingTask(102L, offboardingCase, TaskCategory.FINANCE, "Settle Dues", TaskStatus.IN_PROGRESS);
        List<OffboardingTask> tasks = Arrays.asList(task1, task2);

        String expectedOutstandingString = "IT - Return Laptop - PENDING\nFINANCE - Settle Dues - IN_PROGRESS";

        ClearanceCertificate savedCertificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.PENDING_ITEMS, expectedOutstandingString);
        ClearanceCertificateResponse expectedResponse = createResponse(100L, 1L, 2L, ClearanceStatus.PENDING_ITEMS, expectedOutstandingString);

        when(certificateRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(issuedBy));
        when(taskRepository.findByOffboardingCase_CaseId(1L)).thenReturn(tasks);
        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(offboardingCase);
        when(certificateRepository.save(any(ClearanceCertificate.class))).thenReturn(savedCertificate);
        when(mapper.toResponse(savedCertificate)).thenReturn(expectedResponse);

        ClearanceCertificateResponse response = service.issueCertificate(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        // Verify OffboardingCase status updated to CLEARANCE_PENDING
        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
        verify(offboardingCaseRepository).save(caseCaptor.capture());
        assertEquals(OffboardingStatus.CLEARANCE_PENDING, caseCaptor.getValue().getStatus());

        // Verify ClearanceCertificate details
        ArgumentCaptor<ClearanceCertificate> certCaptor = ArgumentCaptor.forClass(ClearanceCertificate.class);
        verify(certificateRepository).save(certCaptor.capture());
        ClearanceCertificate capturedCert = certCaptor.getValue();
        assertEquals(offboardingCase, capturedCert.getOffboardingCase());
        assertEquals(issuedBy, capturedCert.getIssuedBy());
        assertEquals(ClearanceStatus.PENDING_ITEMS, capturedCert.getStatus());
        assertEquals(expectedOutstandingString, capturedCert.getOutstandingItems());

        // Verify Audit interaction
        verify(auditLogService).record("EMP2", "ISSUE_CLEARANCE_CERTIFICATE", "CLEARANCE");
    }

    // --- getCertificateById Tests ---

    @Test
    void getCertificateById_Success() {
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.COMPLETED);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);
        ClearanceCertificate certificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse expectedResponse = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.findById(100L)).thenReturn(Optional.of(certificate));
        when(mapper.toResponse(certificate)).thenReturn(expectedResponse);

        ClearanceCertificateResponse response = service.getCertificateById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(certificateRepository).findById(100L);
        verify(mapper).toResponse(certificate);
        verifyNoMoreInteractions(certificateRepository, mapper);
    }

    @Test
    void getCertificateById_ThrowsException_WhenNotFound() {
        when(certificateRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getCertificateById(100L);
        });

        assertEquals("Clearance certificate not found with ID: 100", exception.getMessage());
        verify(certificateRepository).findById(100L);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper);
    }

    // --- getCertificateByCase Tests ---

    @Test
    void getCertificateByCase_Success() {
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.COMPLETED);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);
        ClearanceCertificate certificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse expectedResponse = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.of(certificate));
        when(mapper.toResponse(certificate)).thenReturn(expectedResponse);

        ClearanceCertificateResponse response = service.getCertificateByCase(1L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(certificateRepository).findByOffboardingCase_CaseId(1L);
        verify(mapper).toResponse(certificate);
        verifyNoMoreInteractions(certificateRepository, mapper);
    }

    @Test
    void getCertificateByCase_ThrowsException_WhenNotFound() {
        when(certificateRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getCertificateByCase(1L);
        });

        assertEquals("Certificate not found for case ID: 1", exception.getMessage());
        verify(certificateRepository).findByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper);
    }

    // --- getAllCertificates Tests ---

    @Test
    void getAllCertificates_Success() {
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase case1 = createOffboardingCase(1L, employee, OffboardingStatus.COMPLETED);
        OffboardingCase case2 = createOffboardingCase(2L, employee, OffboardingStatus.CLEARANCE_PENDING);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);

        ClearanceCertificate cert1 = createClearanceCertificate(100L, case1, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificate cert2 = createClearanceCertificate(200L, case2, issuedBy, ClearanceStatus.PENDING_ITEMS, "Outstanding items");

        ClearanceCertificateResponse resp1 = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse resp2 = createResponse(200L, 2L, 2L, ClearanceStatus.PENDING_ITEMS, "Outstanding items");

        when(certificateRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2));
        when(mapper.toResponse(cert1)).thenReturn(resp1);
        when(mapper.toResponse(cert2)).thenReturn(resp2);

        List<ClearanceCertificateResponse> responseList = service.getAllCertificates();

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(resp1, responseList.get(0));
        assertEquals(resp2, responseList.get(1));

        verify(certificateRepository).findAll();
        verify(mapper).toResponse(cert1);
        verify(mapper).toResponse(cert2);
        verifyNoMoreInteractions(certificateRepository, mapper);
    }

    @Test
    void getAllCertificates_Empty() {
        when(certificateRepository.findAll()).thenReturn(Collections.emptyList());

        List<ClearanceCertificateResponse> responseList = service.getAllCertificates();

        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());

        verify(certificateRepository).findAll();
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper);
    }

    // --- getCertificatesByStatus Tests ---

    @Test
    void getCertificatesByStatus_Success() {
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase case1 = createOffboardingCase(1L, employee, OffboardingStatus.COMPLETED);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);

        ClearanceCertificate cert1 = createClearanceCertificate(100L, case1, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");
        ClearanceCertificateResponse resp1 = createResponse(100L, 1L, 2L, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.findByStatus(ClearanceStatus.CLEAR)).thenReturn(Collections.singletonList(cert1));
        when(mapper.toResponse(cert1)).thenReturn(resp1);

        List<ClearanceCertificateResponse> responseList = service.getCertificatesByStatus(ClearanceStatus.CLEAR);

        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        assertEquals(resp1, responseList.get(0));

        verify(certificateRepository).findByStatus(ClearanceStatus.CLEAR);
        verify(mapper).toResponse(cert1);
        verifyNoMoreInteractions(certificateRepository, mapper);
    }

    @Test
    void getCertificatesByStatus_Empty() {
        when(certificateRepository.findByStatus(ClearanceStatus.CLEAR)).thenReturn(Collections.emptyList());

        List<ClearanceCertificateResponse> responseList = service.getCertificatesByStatus(ClearanceStatus.CLEAR);

        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());

        verify(certificateRepository).findByStatus(ClearanceStatus.CLEAR);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper);
    }

    // --- deleteCertificate Tests ---

    @Test
    void deleteCertificate_Success() {
        User employee = createUser(10L, "Employee", UserRole.EMPLOYEE);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, OffboardingStatus.COMPLETED);
        User issuedBy = createUser(2L, "Admin Issuer", UserRole.ADMIN);
        ClearanceCertificate certificate = createClearanceCertificate(100L, offboardingCase, issuedBy, ClearanceStatus.CLEAR, "No outstanding items");

        when(certificateRepository.findById(100L)).thenReturn(Optional.of(certificate));
        doNothing().when(certificateRepository).delete(certificate);

        service.deleteCertificate(100L);

        verify(certificateRepository).findById(100L);
        verify(certificateRepository).delete(certificate);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    @Test
    void deleteCertificate_ThrowsException_WhenNotFound() {
        when(certificateRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteCertificate(100L);
        });

        assertEquals("Clearance certificate not found with ID: 100", exception.getMessage());
        verify(certificateRepository).findById(100L);
        verifyNoMoreInteractions(certificateRepository);
        verifyNoInteractions(mapper, auditLogService);
    }
}
