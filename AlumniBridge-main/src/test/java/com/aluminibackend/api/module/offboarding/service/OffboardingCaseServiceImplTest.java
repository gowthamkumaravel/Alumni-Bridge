//package com.aluminibackend.api.module.offboarding.service;
//
//import com.aluminibackend.api.module.notification.enums.NotificationCategory;
//import com.aluminibackend.api.module.notification.service.NotificationService;
//import com.aluminibackend.api.module.audit.service.AuditLogService;
//import com.aluminibackend.api.module.offboarding.dto.request.CreateOffboardingCaseRequest;
//import com.aluminibackend.api.module.offboarding.dto.request.UpdateOffboardingCaseRequest;
//import com.aluminibackend.api.module.offboarding.dto.response.OffboardingCaseResponse;
//import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
//import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
//import com.aluminibackend.api.module.offboarding.enums.DepartureType;
//import com.aluminibackend.api.module.offboarding.mapper.OffboardingCaseMapper;
//import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
//import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
//import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
//import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
//import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
//import com.aluminibackend.api.module.user.entity.User;
//import com.aluminibackend.api.module.user.enums.UserRole;
//import com.aluminibackend.api.module.user.enums.AccountType;
//import com.aluminibackend.api.module.user.enums.UserStatus;
//import com.aluminibackend.api.module.user.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class OffboardingCaseServiceImplTest {
//
//    @Mock
//    private OffboardingCaseRepository offboardingCaseRepository;
//
//    @Mock
//    private OffboardingTaskRepository offboardingTaskRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private OffboardingCaseMapper offboardingCaseMapper;
//
//    @Mock
//    private NotificationService notificationService;
//
//    @Mock
//    private AuditLogService auditLogService;
//
//    @InjectMocks
//    private OffboardingCaseServiceImpl service;
//
//    // --- Helper Methods ---
//
//    private User createUser(Long userId, String name, UserRole role) {
//        return User.builder()
//                .userId(userId)
//                .name(name)
//                .employeeId("EMP" + userId)
//                .email(name.toLowerCase().replace(" ", "") + "@example.com")
//                .role(role)
//                .password("password")
//                .accountType(AccountType.ACTIVE)
//                .status(UserStatus.ACTIVE)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    private OffboardingCase createCaseEntity(Long caseId, User employee, User hrbpOwner, DepartureType departureType, OffboardingStatus status) {
//        return OffboardingCase.builder()
//                .caseId(caseId)
//                .employee(employee)
//                .departureType(departureType)
//                .lastWorkingDate(LocalDate.now().plusDays(30))
//                .hrbpOwner(hrbpOwner)
//                .status(status)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    private CreateOffboardingCaseRequest createCreateRequest(String employeeId, Long hrbpOwnerId, DepartureType departureType, LocalDate lastWorkingDate) {
//        CreateOffboardingCaseRequest request = new CreateOffboardingCaseRequest();
//        request.setEmployeeId(employeeId);
//        request.setHrbpOwnerId(hrbpOwnerId);
//        request.setDepartureType(departureType);
//        request.setLastWorkingDate(lastWorkingDate);
//        return request;
//    }
//
//    private UpdateOffboardingCaseRequest createUpdateRequest(DepartureType departureType, LocalDate lastWorkingDate, Long hrbpOwnerId, OffboardingStatus status) {
//        UpdateOffboardingCaseRequest request = new UpdateOffboardingCaseRequest();
//        request.setDepartureType(departureType);
//        request.setLastWorkingDate(lastWorkingDate);
//        request.setHrbpOwnerId(hrbpOwnerId);
//        request.setStatus(status);
//        return request;
//    }
//
//    private OffboardingCaseResponse createResponse(Long caseId, Long employeeId, Long hrbpOwnerId, DepartureType departureType, OffboardingStatus status) {
//        return OffboardingCaseResponse.builder()
//                .caseId(caseId)
//                .employeeId(employeeId)
//                .employeeName("Employee Name")
//                .employeeEmail("employee@example.com")
//                .departureType(departureType)
//                .lastWorkingDate(LocalDate.now().plusDays(30))
//                .hrbpOwnerId(hrbpOwnerId)
//                .hrbpOwnerName("HRBP Name")
//                .status(status)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    // --- createCase Tests ---
//
//    @Test
//    void createCase_ThrowsException_WhenActiveCaseAlreadyExists() {
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, LocalDate.now().plusDays(30));
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(true);
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.createCase(request);
//        });
//
//        assertEquals("Active offboarding case already exists for this employee", exception.getMessage());
//        verify(offboardingCaseRepository).existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED);
//        verifyNoMoreInteractions(offboardingCaseRepository);
//        verifyNoInteractions(userRepository, offboardingTaskRepository, offboardingCaseMapper, notificationService, auditLogService);
//    }
//
//    @Test
//    void createCase_ThrowsException_WhenEmployeeNotFound() {
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, LocalDate.now().plusDays(30));
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.createCase(request);
//        });
//
//        assertEquals("Employee not found", exception.getMessage());
//        verify(offboardingCaseRepository).existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED);
//        verify(userRepository).findById(1L);
//        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
//        verifyNoInteractions(offboardingTaskRepository, offboardingCaseMapper, notificationService, auditLogService);
//    }
//
//    @Test
//    void createCase_ThrowsException_WhenHrbpOwnerNotFound() {
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, LocalDate.now().plusDays(30));
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
//        when(userRepository.findById(2L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.createCase(request);
//        });
//
//        assertEquals("HRBP owner not found", exception.getMessage());
//        verify(offboardingCaseRepository).existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED);
//        verify(userRepository).findById(1L);
//        verify(userRepository).findById(2L);
//        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
//        verifyNoInteractions(offboardingTaskRepository, offboardingCaseMapper, notificationService, auditLogService);
//    }
//
//    @Test
//    void createCase_ThrowsException_WhenHrbpOwnerHasInvalidRole() {
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, LocalDate.now().plusDays(30));
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User invalidHrbp = createUser(2L, "Invalid HRBP", UserRole.EMPLOYEE); // Not HRBP or ADMIN
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
//        when(userRepository.findById(2L)).thenReturn(Optional.of(invalidHrbp));
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.createCase(request);
//        });
//
//        assertEquals("HRBP owner must have HRBP or ADMIN role", exception.getMessage());
//        verify(offboardingCaseRepository).existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED);
//        verify(userRepository).findById(1L);
//        verify(userRepository).findById(2L);
//        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
//        verifyNoInteractions(offboardingTaskRepository, offboardingCaseMapper, notificationService, auditLogService);
//    }
//
//    @Test
//    void createCase_Success_WithItOpsUserFound() {
//        LocalDate lastWorkingDate = LocalDate.now().plusDays(30);
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, lastWorkingDate);
//        User employee = createUser(1L, "John Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "Jane HRBP", UserRole.HRBP);
//        User itOps = createUser(3L, "IT Operator", UserRole.IT_OPS);
//
//        OffboardingCase savedCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        savedCase.setLastWorkingDate(lastWorkingDate);
//
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        expectedResponse.setLastWorkingDate(lastWorkingDate);
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
//        when(userRepository.findById(2L)).thenReturn(Optional.of(hrbp));
//        when(userRepository.findByRole(UserRole.IT_OPS)).thenReturn(Collections.singletonList(itOps));
//        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(savedCase);
//        when(offboardingCaseMapper.toResponse(savedCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.createCase(request);
//
//        assertNotNull(response);
//        assertEquals(expectedResponse, response);
//
//        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
//        verify(offboardingCaseRepository).save(caseCaptor.capture());
//        OffboardingCase capturedCase = caseCaptor.getValue();
//        assertEquals(employee, capturedCase.getEmployee());
//        assertEquals(hrbp, capturedCase.getHrbpOwner());
//        assertEquals(DepartureType.RESIGNATION, capturedCase.getDepartureType());
//        assertEquals(lastWorkingDate, capturedCase.getLastWorkingDate());
//        assertEquals(OffboardingStatus.INITIATED, capturedCase.getStatus());
//
//        ArgumentCaptor<OffboardingTask> taskCaptor = ArgumentCaptor.forClass(OffboardingTask.class);
//        verify(offboardingTaskRepository, times(7)).save(taskCaptor.capture());
//        List<OffboardingTask> savedTasks = taskCaptor.getAllValues();
//        assertEquals(7, savedTasks.size());
//
//        // Task 1: HR Complete exit survey
//        OffboardingTask task1 = savedTasks.get(0);
//        assertEquals(savedCase, task1.getOffboardingCase());
//        assertEquals(TaskCategory.HR, task1.getCategory());
//        assertEquals("Complete exit survey", task1.getTaskDescription());
//        assertEquals(employee, task1.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(5), task1.getDueDate());
//        assertEquals(TaskStatus.PENDING, task1.getStatus());
//
//        // Task 2: HR Register/confirm alumni pref
//        OffboardingTask task2 = savedTasks.get(1);
//        assertEquals(TaskCategory.HR, task2.getCategory());
//        assertEquals("Register or confirm alumni community preference", task2.getTaskDescription());
//        assertEquals(employee, task2.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(3), task2.getDueDate());
//
//        // Task 3: MANAGER Submit handover notes
//        OffboardingTask task3 = savedTasks.get(2);
//        assertEquals(TaskCategory.MANAGER, task3.getCategory());
//        assertEquals("Submit knowledge handover notes and active project summary", task3.getTaskDescription());
//        assertEquals(employee, task3.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(7), task3.getDueDate());
//
//        // Task 4: IT Revoke access
//        OffboardingTask task4 = savedTasks.get(3);
//        assertEquals(TaskCategory.IT, task4.getCategory());
//        assertEquals("Revoke system access and collect assigned devices", task4.getTaskDescription());
//        assertEquals(itOps, task4.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(2), task4.getDueDate());
//
//        // Task 5: FINANCE Payroll clearance
//        OffboardingTask task5 = savedTasks.get(4);
//        assertEquals(TaskCategory.FINANCE, task5.getCategory());
//        assertEquals("Verify expense, advance, and payroll clearance", task5.getTaskDescription());
//        assertEquals(hrbp, task5.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(2), task5.getDueDate());
//
//        // Task 6: FACILITIES Asset return
//        OffboardingTask task6 = savedTasks.get(5);
//        assertEquals(TaskCategory.FACILITIES, task6.getCategory());
//        assertEquals("Confirm badge, workspace, and physical asset return", task6.getTaskDescription());
//        assertEquals(hrbp, task6.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(2), task6.getDueDate());
//
//        // Task 7: LEGAL NDA clearance
//        OffboardingTask task7 = savedTasks.get(6);
//        assertEquals(TaskCategory.LEGAL, task7.getCategory());
//        assertEquals("Confirm policy, NDA, and compliance clearance", task7.getTaskDescription());
//        assertEquals(hrbp, task7.getAssignedTo());
//        assertEquals(lastWorkingDate.minusDays(1), task7.getDueDate());
//
//        // Notifications
//        verify(notificationService).createSystemNotification(1L, "Your offboarding case has been initiated. Please review your checklist.", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(2L, "Offboarding case created for John Employee.", NotificationCategory.OFFBOARDING);
//
//        verify(notificationService).createSystemNotification(1L, "New offboarding task assigned: Complete exit survey", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(1L, "New offboarding task assigned: Register or confirm alumni community preference", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(1L, "New offboarding task assigned: Submit knowledge handover notes and active project summary", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(3L, "New offboarding task assigned: Revoke system access and collect assigned devices", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(2L, "New offboarding task assigned: Verify expense, advance, and payroll clearance", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(2L, "New offboarding task assigned: Confirm badge, workspace, and physical asset return", NotificationCategory.OFFBOARDING);
//        verify(notificationService).createSystemNotification(2L, "New offboarding task assigned: Confirm policy, NDA, and compliance clearance", NotificationCategory.OFFBOARDING);
//
//        verify(auditLogService).record(2L, "CREATE_OFFBOARDING_CASE", "OFFBOARDING");
//        verify(offboardingCaseMapper).toResponse(savedCase);
//    }
//
//    @Test
//    void createCase_Success_WithItOpsUserNotFound_FallsBackToHrbp() {
//        LocalDate lastWorkingDate = LocalDate.now().plusDays(30);
//        CreateOffboardingCaseRequest request = createCreateRequest(1L, 2L, DepartureType.RESIGNATION, lastWorkingDate);
//        User employee = createUser(1L, "John Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "Jane HRBP", UserRole.HRBP);
//
//        OffboardingCase savedCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        savedCase.setLastWorkingDate(lastWorkingDate);
//
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        expectedResponse.setLastWorkingDate(lastWorkingDate);
//
//        when(offboardingCaseRepository.existsByEmployee_EmployeeIdAndStatusNot(1L, OffboardingStatus.COMPLETED)).thenReturn(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
//        when(userRepository.findById(2L)).thenReturn(Optional.of(hrbp));
//        when(userRepository.findByRole(UserRole.IT_OPS)).thenReturn(Collections.emptyList());
//        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(savedCase);
//        when(offboardingCaseMapper.toResponse(savedCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.createCase(request);
//
//        assertNotNull(response);
//
//        ArgumentCaptor<OffboardingTask> taskCaptor = ArgumentCaptor.forClass(OffboardingTask.class);
//        verify(offboardingTaskRepository, times(7)).save(taskCaptor.capture());
//        List<OffboardingTask> savedTasks = taskCaptor.getAllValues();
//        OffboardingTask task4 = savedTasks.get(3);
//        assertEquals(TaskCategory.IT, task4.getCategory());
//        assertEquals(hrbp, task4.getAssignedTo()); // fell back to hrbp
//
//        verify(notificationService).createSystemNotification(2L, "New offboarding task assigned: Revoke system access and collect assigned devices", NotificationCategory.OFFBOARDING);
//    }
//
//    // --- getCaseById Tests ---
//
//    @Test
//    void getCaseById_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase offboardingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(offboardingCase));
//        when(offboardingCaseMapper.toResponse(offboardingCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.getCaseById(100L);
//
//        assertNotNull(response);
//        assertEquals(expectedResponse, response);
//        verify(offboardingCaseRepository).findById(100L);
//        verify(offboardingCaseMapper).toResponse(offboardingCase);
//    }
//
//    @Test
//    void getCaseById_ThrowsException_WhenNotFound() {
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.getCaseById(100L);
//        });
//
//        assertEquals("Offboarding case not found with ID: 100", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verifyNoInteractions(offboardingCaseMapper);
//    }
//
//    // --- getAllCases Tests ---
//
//    @Test
//    void getAllCases_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase case1 = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCaseResponse resp1 = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findAll()).thenReturn(Collections.singletonList(case1));
//        when(offboardingCaseMapper.toResponse(case1)).thenReturn(resp1);
//
//        List<OffboardingCaseResponse> responses = service.getAllCases();
//
//        assertNotNull(responses);
//        assertEquals(1, responses.size());
//        assertEquals(resp1, responses.get(0));
//        verify(offboardingCaseRepository).findAll();
//        verify(offboardingCaseMapper).toResponse(case1);
//    }
//
//    @Test
//    void getAllCases_Empty() {
//        when(offboardingCaseRepository.findAll()).thenReturn(Collections.emptyList());
//
//        List<OffboardingCaseResponse> responses = service.getAllCases();
//
//        assertNotNull(responses);
//        assertTrue(responses.isEmpty());
//        verify(offboardingCaseRepository).findAll();
//        verifyNoInteractions(offboardingCaseMapper);
//    }
//
//    // --- getCasesByEmployee Tests ---
//
//    @Test
//    void getCasesByEmployee_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase case1 = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCaseResponse resp1 = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findByEmployee_UserId(1L)).thenReturn(Collections.singletonList(case1));
//        when(offboardingCaseMapper.toResponse(case1)).thenReturn(resp1);
//
//        List<OffboardingCaseResponse> responses = service.getCasesByEmployee(1L);
//
//        assertNotNull(responses);
//        assertEquals(1, responses.size());
//        assertEquals(resp1, responses.get(0));
//        verify(offboardingCaseRepository).findByEmployee_UserId(1L);
//        verify(offboardingCaseMapper).toResponse(case1);
//    }
//
//    @Test
//    void getCasesByEmployee_Empty() {
//        when(offboardingCaseRepository.findByEmployee_UserId(1L)).thenReturn(Collections.emptyList());
//
//        List<OffboardingCaseResponse> responses = service.getCasesByEmployee(1L);
//
//        assertNotNull(responses);
//        assertTrue(responses.isEmpty());
//        verify(offboardingCaseRepository).findByEmployee_UserId(1L);
//        verifyNoInteractions(offboardingCaseMapper);
//    }
//
//    // --- getCasesByHrbpOwner Tests ---
//
//    @Test
//    void getCasesByHrbpOwner_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase case1 = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCaseResponse resp1 = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findByHrbpOwner_UserId(2L)).thenReturn(Collections.singletonList(case1));
//        when(offboardingCaseMapper.toResponse(case1)).thenReturn(resp1);
//
//        List<OffboardingCaseResponse> responses = service.getCasesByHrbpOwner(2L);
//
//        assertNotNull(responses);
//        assertEquals(1, responses.size());
//        assertEquals(resp1, responses.get(0));
//        verify(offboardingCaseRepository).findByHrbpOwner_UserId(2L);
//        verify(offboardingCaseMapper).toResponse(case1);
//    }
//
//    @Test
//    void getCasesByHrbpOwner_Empty() {
//        when(offboardingCaseRepository.findByHrbpOwner_UserId(2L)).thenReturn(Collections.emptyList());
//
//        List<OffboardingCaseResponse> responses = service.getCasesByHrbpOwner(2L);
//
//        assertNotNull(responses);
//        assertTrue(responses.isEmpty());
//        verify(offboardingCaseRepository).findByHrbpOwner_UserId(2L);
//        verifyNoInteractions(offboardingCaseMapper);
//    }
//
//    // --- getCasesByStatus Tests ---
//
//    @Test
//    void getCasesByStatus_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase case1 = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCaseResponse resp1 = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findByStatus(OffboardingStatus.INITIATED)).thenReturn(Collections.singletonList(case1));
//        when(offboardingCaseMapper.toResponse(case1)).thenReturn(resp1);
//
//        List<OffboardingCaseResponse> responses = service.getCasesByStatus(OffboardingStatus.INITIATED);
//
//        assertNotNull(responses);
//        assertEquals(1, responses.size());
//        assertEquals(resp1, responses.get(0));
//        verify(offboardingCaseRepository).findByStatus(OffboardingStatus.INITIATED);
//        verify(offboardingCaseMapper).toResponse(case1);
//    }
//
//    @Test
//    void getCasesByStatus_Empty() {
//        when(offboardingCaseRepository.findByStatus(OffboardingStatus.INITIATED)).thenReturn(Collections.emptyList());
//
//        List<OffboardingCaseResponse> responses = service.getCasesByStatus(OffboardingStatus.INITIATED);
//
//        assertNotNull(responses);
//        assertTrue(responses.isEmpty());
//        verify(offboardingCaseRepository).findByStatus(OffboardingStatus.INITIATED);
//        verifyNoInteractions(offboardingCaseMapper);
//    }
//
//    // --- updateCase Tests ---
//
//    @Test
//    void updateCase_ThrowsException_WhenCaseNotFound() {
//        UpdateOffboardingCaseRequest request = createUpdateRequest(DepartureType.RETIREMENT, LocalDate.now().plusDays(20), 4L, OffboardingStatus.IN_PROGRESS);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.updateCase(100L, request);
//        });
//
//        assertEquals("Offboarding case not found with ID: 100", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verifyNoMoreInteractions(offboardingCaseRepository);
//        verifyNoInteractions(userRepository, offboardingCaseMapper, auditLogService);
//    }
//
//    @Test
//    void updateCase_ThrowsException_WhenHrbpOwnerNotFound() {
//        UpdateOffboardingCaseRequest request = createUpdateRequest(DepartureType.RETIREMENT, LocalDate.now().plusDays(20), 4L, OffboardingStatus.IN_PROGRESS);
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase existingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        when(userRepository.findById(4L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.updateCase(100L, request);
//        });
//
//        assertEquals("HRBP owner not found", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verify(userRepository).findById(4L);
//        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
//        verifyNoInteractions(offboardingCaseMapper, auditLogService);
//    }
//
//    @Test
//    void updateCase_ThrowsException_WhenHrbpOwnerRoleInvalid() {
//        UpdateOffboardingCaseRequest request = createUpdateRequest(DepartureType.RETIREMENT, LocalDate.now().plusDays(20), 4L, OffboardingStatus.IN_PROGRESS);
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase existingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        User invalidHrbp = createUser(4L, "Employee HRBP", UserRole.EMPLOYEE);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        when(userRepository.findById(4L)).thenReturn(Optional.of(invalidHrbp));
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.updateCase(100L, request);
//        });
//
//        assertEquals("HRBP owner must have HRBP or ADMIN role", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verify(userRepository).findById(4L);
//        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
//        verifyNoInteractions(offboardingCaseMapper, auditLogService);
//    }
//
//    @Test
//    void updateCase_Success_AllFields() {
//        LocalDate newDate = LocalDate.now().plusDays(20);
//        UpdateOffboardingCaseRequest request = createUpdateRequest(DepartureType.RETIREMENT, newDate, 4L, OffboardingStatus.IN_PROGRESS);
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User oldHrbp = createUser(2L, "Old HRBP", UserRole.HRBP);
//        User newHrbp = createUser(4L, "New Admin Owner", UserRole.ADMIN);
//
//        OffboardingCase existingCase = createCaseEntity(100L, employee, oldHrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        OffboardingCase updatedCase = createCaseEntity(100L, employee, newHrbp, DepartureType.RETIREMENT, OffboardingStatus.IN_PROGRESS);
//        updatedCase.setLastWorkingDate(newDate);
//
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 4L, DepartureType.RETIREMENT, OffboardingStatus.IN_PROGRESS);
//        expectedResponse.setLastWorkingDate(newDate);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        when(userRepository.findById(4L)).thenReturn(Optional.of(newHrbp));
//        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(updatedCase);
//        when(offboardingCaseMapper.toResponse(updatedCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.updateCase(100L, request);
//
//        assertNotNull(response);
//        assertEquals(expectedResponse, response);
//
//        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
//        verify(offboardingCaseRepository).save(caseCaptor.capture());
//        OffboardingCase captured = caseCaptor.getValue();
//        assertEquals(DepartureType.RETIREMENT, captured.getDepartureType());
//        assertEquals(newDate, captured.getLastWorkingDate());
//        assertEquals(newHrbp, captured.getHrbpOwner());
//        assertEquals(OffboardingStatus.IN_PROGRESS, captured.getStatus());
//
//        verify(auditLogService).record(4L, "UPDATE_OFFBOARDING_CASE", "OFFBOARDING");
//        verify(offboardingCaseMapper).toResponse(updatedCase);
//    }
//
//    @Test
//    void updateCase_Success_NullFields_HrbpOwnerNotValidated() {
//        UpdateOffboardingCaseRequest request = createUpdateRequest(null, null, null, null);
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase existingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        when(offboardingCaseRepository.save(existingCase)).thenReturn(existingCase);
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        when(offboardingCaseMapper.toResponse(existingCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.updateCase(100L, request);
//
//        assertNotNull(response);
//        assertEquals(expectedResponse, response);
//        verify(offboardingCaseRepository).save(existingCase);
//        verify(userRepository, never()).findById(anyLong());
//        verify(auditLogService).record(2L, "UPDATE_OFFBOARDING_CASE", "OFFBOARDING");
//    }
//
//    // --- updateStatus Tests ---
//
//    @Test
//    void updateStatus_ThrowsException_WhenCaseNotFound() {
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.updateStatus(100L, OffboardingStatus.COMPLETED);
//        });
//
//        assertEquals("Offboarding case not found with ID: 100", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verifyNoMoreInteractions(offboardingCaseRepository);
//        verifyNoInteractions(auditLogService, offboardingCaseMapper);
//    }
//
//    @Test
//    void updateStatus_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase existingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//        OffboardingCase updatedCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.COMPLETED);
//        OffboardingCaseResponse expectedResponse = createResponse(100L, 1L, 2L, DepartureType.RESIGNATION, OffboardingStatus.COMPLETED);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        when(offboardingCaseRepository.save(any(OffboardingCase.class))).thenReturn(updatedCase);
//        when(offboardingCaseMapper.toResponse(updatedCase)).thenReturn(expectedResponse);
//
//        OffboardingCaseResponse response = service.updateStatus(100L, OffboardingStatus.COMPLETED);
//
//        assertNotNull(response);
//        assertEquals(expectedResponse, response);
//
//        ArgumentCaptor<OffboardingCase> caseCaptor = ArgumentCaptor.forClass(OffboardingCase.class);
//        verify(offboardingCaseRepository).save(caseCaptor.capture());
//        assertEquals(OffboardingStatus.COMPLETED, caseCaptor.getValue().getStatus());
//
//        verify(auditLogService).record(2L, "UPDATE_OFFBOARDING_STATUS_COMPLETED", "OFFBOARDING");
//        verify(offboardingCaseMapper).toResponse(updatedCase);
//    }
//
//    // --- deleteCase Tests ---
//
//    @Test
//    void deleteCase_ThrowsException_WhenCaseNotFound() {
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            service.deleteCase(100L);
//        });
//
//        assertEquals("Offboarding case not found with ID: 100", exception.getMessage());
//        verify(offboardingCaseRepository).findById(100L);
//        verify(offboardingCaseRepository, never()).delete(any());
//    }
//
//    @Test
//    void deleteCase_Success() {
//        User employee = createUser(1L, "Employee", UserRole.EMPLOYEE);
//        User hrbp = createUser(2L, "HRBP", UserRole.HRBP);
//        OffboardingCase existingCase = createCaseEntity(100L, employee, hrbp, DepartureType.RESIGNATION, OffboardingStatus.INITIATED);
//
//        when(offboardingCaseRepository.findById(100L)).thenReturn(Optional.of(existingCase));
//        doNothing().when(offboardingCaseRepository).delete(existingCase);
//
//        service.deleteCase(100L);
//
//        verify(offboardingCaseRepository).findById(100L);
//        verify(offboardingCaseRepository).delete(existingCase);
//    }
//}
