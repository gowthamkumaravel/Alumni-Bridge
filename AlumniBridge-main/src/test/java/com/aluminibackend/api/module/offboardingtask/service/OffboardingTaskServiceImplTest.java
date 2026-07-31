package com.aluminibackend.api.module.offboardingtask.service;

import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.offboardingtask.dto.request.CreateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.dto.request.UpdateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.dto.response.OffboardingTaskResponse;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import com.aluminibackend.api.module.offboardingtask.mapper.OffboardingTaskMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffboardingTaskServiceImplTest {

    @Mock
    private OffboardingTaskRepository taskRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OffboardingTaskMapper mapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OffboardingTaskServiceImpl service;

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

    private OffboardingTask createOffboardingTask(Long taskId, OffboardingCase offboardingCase, User assignedTo, TaskCategory category, String description, LocalDate dueDate, TaskStatus status) {
        return OffboardingTask.builder()
                .taskId(taskId)
                .offboardingCase(offboardingCase)
                .category(category)
                .taskDescription(description)
                .assignedTo(assignedTo)
                .dueDate(dueDate)
                .status(status)
                .build();
    }

    private CreateOffboardingTaskRequest createCreateRequest(Long caseId, TaskCategory category, String description, Long assignedToId, LocalDate dueDate) {
        CreateOffboardingTaskRequest request = new CreateOffboardingTaskRequest();
        request.setCaseId(caseId);
        request.setCategory(category);
        request.setTaskDescription(description);
        request.setAssignedToId(assignedToId);
        request.setDueDate(dueDate);
        return request;
    }

    private UpdateOffboardingTaskRequest createUpdateRequest(String description, Long assignedToId, LocalDate dueDate, TaskStatus status) {
        UpdateOffboardingTaskRequest request = new UpdateOffboardingTaskRequest();
        request.setTaskDescription(description);
        request.setAssignedToId(assignedToId);
        request.setDueDate(dueDate);
        request.setStatus(status);
        return request;
    }

    private OffboardingTaskResponse createResponse(Long taskId, Long caseId, TaskCategory category, String description, Long assignedToId, String assignedToName, LocalDate dueDate, LocalDate completedDate, TaskStatus status) {
        return OffboardingTaskResponse.builder()
                .taskId(taskId)
                .caseId(caseId)
                .category(category)
                .taskDescription(description)
                .assignedToId(assignedToId)
                .assignedToName(assignedToName)
                .dueDate(dueDate)
                .completedDate(completedDate)
                .status(status)
                .build();
    }

    // --- createTask Tests ---

    @Test
    void createTask_Success() {
        CreateOffboardingTaskRequest request = createCreateRequest(1L, TaskCategory.IT, "Collect Access Card", 2L, LocalDate.now().plusDays(3));
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignedUser = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask savedTask = createOffboardingTask(100L, offboardingCase, assignedUser, TaskCategory.IT, "Collect Access Card", request.getDueDate(), TaskStatus.PENDING);
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "Collect Access Card", 2L, "IT Admin", request.getDueDate(), null, TaskStatus.PENDING);

        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedUser));
        when(taskRepository.save(any(OffboardingTask.class))).thenReturn(savedTask);
        when(mapper.toResponse(savedTask)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.createTask(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<OffboardingTask> taskCaptor = ArgumentCaptor.forClass(OffboardingTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        OffboardingTask capturedTask = taskCaptor.getValue();
        assertEquals(offboardingCase, capturedTask.getOffboardingCase());
        assertEquals(TaskCategory.IT, capturedTask.getCategory());
        assertEquals("Collect Access Card", capturedTask.getTaskDescription());
        assertEquals(assignedUser, capturedTask.getAssignedTo());
        assertEquals(request.getDueDate(), capturedTask.getDueDate());
        assertEquals(TaskStatus.PENDING, capturedTask.getStatus());

        verify(notificationService).createSystemNotification(
                "EMP2",
                "New offboarding task assigned: Collect Access Card",
                NotificationCategory.OFFBOARDING
        );
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(mapper).toResponse(savedTask);
        verifyNoMoreInteractions(taskRepository, offboardingCaseRepository, userRepository, mapper, notificationService);
    }

    @Test
    void createTask_ThrowsException_WhenCaseNotFound() {
        CreateOffboardingTaskRequest request = createCreateRequest(1L, TaskCategory.IT, "Collect Access Card", 2L, LocalDate.now().plusDays(3));

        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createTask(request);
        });

        assertEquals("Offboarding Case not found", exception.getMessage());
        verify(offboardingCaseRepository).findById(1L);
        verifyNoMoreInteractions(offboardingCaseRepository);
        verifyNoInteractions(userRepository, taskRepository, mapper, notificationService);
    }

    @Test
    void createTask_ThrowsException_WhenAssignedUserNotFound() {
        CreateOffboardingTaskRequest request = createCreateRequest(1L, TaskCategory.IT, "Collect Access Card", 2L, LocalDate.now().plusDays(3));
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);

        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createTask(request);
        });

        assertEquals("Assigned User not found", exception.getMessage());
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(offboardingCaseRepository, userRepository);
        verifyNoInteractions(taskRepository, mapper, notificationService);
    }

    // --- getTask Tests ---

    @Test
    void getTask_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignedUser = createUser(2L, "IT Admin", UserRole.ADMIN);
        OffboardingTask task = createOffboardingTask(100L, offboardingCase, assignedUser, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "Collect Access Card", 2L, "IT Admin", LocalDate.now(), null, TaskStatus.PENDING);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(mapper.toResponse(task)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.getTask(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(taskRepository).findById(100L);
        verify(mapper).toResponse(task);
        verifyNoMoreInteractions(taskRepository, mapper);
    }

    @Test
    void getTask_ThrowsException_WhenTaskNotFound() {
        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getTask(100L);
        });

        assertEquals("Task not found with id : 100", exception.getMessage());
        verify(taskRepository).findById(100L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(mapper);
    }

    // --- getTasksByCase Tests ---

    @Test
    void getTasksByCase_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignedUser = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask task1 = createOffboardingTask(100L, offboardingCase, assignedUser, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTask task2 = createOffboardingTask(101L, offboardingCase, assignedUser, TaskCategory.FACILITIES, "Clear Locker", LocalDate.now(), TaskStatus.COMPLETED);

        OffboardingTaskResponse response1 = createResponse(100L, 1L, TaskCategory.IT, "Collect Access Card", 2L, "IT Admin", LocalDate.now(), null, TaskStatus.PENDING);
        OffboardingTaskResponse response2 = createResponse(101L, 1L, TaskCategory.FACILITIES, "Clear Locker", 2L, "IT Admin", LocalDate.now(), LocalDate.now(), TaskStatus.COMPLETED);

        when(taskRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(mapper.toResponse(task1)).thenReturn(response1);
        when(mapper.toResponse(task2)).thenReturn(response2);

        List<OffboardingTaskResponse> results = service.getTasksByCase(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(response1, results.get(0));
        assertEquals(response2, results.get(1));

        verify(taskRepository).findByOffboardingCase_CaseId(1L);
        verify(mapper).toResponse(task1);
        verify(mapper).toResponse(task2);
        verifyNoMoreInteractions(taskRepository, mapper);
    }

    @Test
    void getTasksByCase_Empty() {
        when(taskRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Collections.emptyList());

        List<OffboardingTaskResponse> results = service.getTasksByCase(1L);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(taskRepository).findByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(mapper);
    }

    // --- getTasksByAssignee Tests ---

    @Test
    void getTasksByAssignee_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignedUser = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask task = createOffboardingTask(100L, offboardingCase, assignedUser, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTaskResponse responseDto = createResponse(100L, 1L, TaskCategory.IT, "Collect Access Card", 2L, "IT Admin", LocalDate.now(), null, TaskStatus.PENDING);

        when(taskRepository.findByAssignedTo_UserId(2L)).thenReturn(Collections.singletonList(task));
        when(mapper.toResponse(task)).thenReturn(responseDto);

        List<OffboardingTaskResponse> results = service.getTasksByAssignee(2L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));

        verify(taskRepository).findByAssignedTo_UserId(2L);
        verify(mapper).toResponse(task);
        verifyNoMoreInteractions(taskRepository, mapper);
    }

    @Test
    void getTasksByAssignee_Empty() {
        when(taskRepository.findByAssignedTo_UserId(2L)).thenReturn(Collections.emptyList());

        List<OffboardingTaskResponse> results = service.getTasksByAssignee(2L);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(taskRepository).findByAssignedTo_UserId(2L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(mapper);
    }

    // --- updateTask Tests ---

    @Test
    void updateTask_Success_AllFieldsChanged() {
        UpdateOffboardingTaskRequest request = createUpdateRequest("New Description", 3L, LocalDate.now().plusDays(10), TaskStatus.IN_PROGRESS);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User oldAssignee = createUser(2L, "Old Assignee", UserRole.ADMIN);
        User newAssignee = createUser(3L, "New Assignee", UserRole.HRBP);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, oldAssignee, TaskCategory.IT, "Old Description", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTask updatedTask = createOffboardingTask(100L, offboardingCase, newAssignee, TaskCategory.IT, "New Description", request.getDueDate(), TaskStatus.IN_PROGRESS);
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "New Description", 3L, "New Assignee", request.getDueDate(), null, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(mapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.updateTask(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals("New Description", existingTask.getTaskDescription());
        assertEquals(newAssignee, existingTask.getAssignedTo());
        assertEquals(request.getDueDate(), existingTask.getDueDate());
        assertEquals(TaskStatus.IN_PROGRESS, existingTask.getStatus());
        assertNull(existingTask.getCompletedDate());

        verify(taskRepository).findById(100L);
        verify(userRepository).findById(3L);
        verify(taskRepository).save(existingTask);
        verify(mapper).toResponse(updatedTask);
        verifyNoMoreInteractions(taskRepository, userRepository, mapper);
    }

    @Test
    void updateTask_Success_StatusCompleted() {
        UpdateOffboardingTaskRequest request = createUpdateRequest(null, null, null, TaskStatus.COMPLETED);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignee = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Description", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTask updatedTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Description", LocalDate.now(), TaskStatus.COMPLETED);
        updatedTask.setCompletedDate(LocalDate.now());
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "Description", 2L, "IT Admin", LocalDate.now(), LocalDate.now(), TaskStatus.COMPLETED);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(mapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.updateTask(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(TaskStatus.COMPLETED, existingTask.getStatus());
        assertEquals(LocalDate.now(), existingTask.getCompletedDate());

        verify(taskRepository).findById(100L);
        verify(taskRepository).save(existingTask);
        verify(mapper).toResponse(updatedTask);
        verifyNoMoreInteractions(taskRepository, mapper);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateTask_Success_NullFields() {
        UpdateOffboardingTaskRequest request = createUpdateRequest(null, null, null, null);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignee = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Description", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "Description", 2L, "IT Admin", LocalDate.now(), null, TaskStatus.PENDING);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(mapper.toResponse(existingTask)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.updateTask(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals("Description", existingTask.getTaskDescription());
        assertEquals(assignee, existingTask.getAssignedTo());
        assertEquals(LocalDate.now(), existingTask.getDueDate());
        assertEquals(TaskStatus.PENDING, existingTask.getStatus());
        assertNull(existingTask.getCompletedDate());

        verify(taskRepository).findById(100L);
        verify(taskRepository).save(existingTask);
        verify(mapper).toResponse(existingTask);
        verifyNoMoreInteractions(taskRepository, mapper);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateTask_ThrowsException_WhenTaskNotFound() {
        UpdateOffboardingTaskRequest request = createUpdateRequest("New Description", 3L, LocalDate.now().plusDays(10), TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateTask(100L, request);
        });

        assertEquals("Task not found with id : 100", exception.getMessage());
        verify(taskRepository).findById(100L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(userRepository, mapper);
    }

    @Test
    void updateTask_ThrowsException_WhenAssignedUserNotFound() {
        UpdateOffboardingTaskRequest request = createUpdateRequest("New Description", 3L, LocalDate.now().plusDays(10), TaskStatus.IN_PROGRESS);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignee = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Description", LocalDate.now(), TaskStatus.PENDING);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateTask(100L, request);
        });

        assertEquals("Assigned User not found", exception.getMessage());
        verify(taskRepository).findById(100L);
        verify(userRepository).findById(3L);
        verifyNoMoreInteractions(taskRepository, userRepository);
        verifyNoInteractions(mapper);
    }

    // --- markCompleted Tests ---

    @Test
    void markCompleted_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignee = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.PENDING);
        OffboardingTask updatedTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.COMPLETED);
        updatedTask.setCompletedDate(LocalDate.now());
        OffboardingTaskResponse expectedResponse = createResponse(100L, 1L, TaskCategory.IT, "Collect Access Card", 2L, "IT Admin", LocalDate.now(), LocalDate.now(), TaskStatus.COMPLETED);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(mapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        OffboardingTaskResponse response = service.markCompleted(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(TaskStatus.COMPLETED, existingTask.getStatus());
        assertEquals(LocalDate.now(), existingTask.getCompletedDate());

        verify(notificationService).createSystemNotification(
                "EMP9",
                "Offboarding task completed: Collect Access Card",
                NotificationCategory.OFFBOARDING
        );
        verify(taskRepository).findById(100L);
        verify(taskRepository).save(existingTask);
        verify(mapper).toResponse(updatedTask);
        verifyNoMoreInteractions(taskRepository, mapper, notificationService);
    }

    @Test
    void markCompleted_ThrowsException_WhenTaskNotFound() {
        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.markCompleted(100L);
        });

        assertEquals("Task not found with id : 100", exception.getMessage());
        verify(taskRepository).findById(100L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(mapper, notificationService);
    }

    // --- deleteTask Tests ---

    @Test
    void deleteTask_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbpOwner = createUser(9L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbpOwner);
        User assignee = createUser(2L, "IT Admin", UserRole.ADMIN);

        OffboardingTask existingTask = createOffboardingTask(100L, offboardingCase, assignee, TaskCategory.IT, "Collect Access Card", LocalDate.now(), TaskStatus.PENDING);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        service.deleteTask(100L);

        verify(taskRepository).findById(100L);
        verify(taskRepository).delete(existingTask);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void deleteTask_ThrowsException_WhenTaskNotFound() {
        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteTask(100L);
        });

        assertEquals("Task not found with id : 100", exception.getMessage());
        verify(taskRepository).findById(100L);
        verifyNoMoreInteractions(taskRepository);
    }
}
