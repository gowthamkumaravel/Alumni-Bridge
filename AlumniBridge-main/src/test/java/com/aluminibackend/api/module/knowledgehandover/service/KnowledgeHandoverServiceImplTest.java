package com.aluminibackend.api.module.knowledgehandover.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.knowledgehandover.dto.request.CreateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.request.UpdateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.response.KnowledgeHandoverResponse;
import com.aluminibackend.api.module.knowledgehandover.entity.KnowledgeHandover;
import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import com.aluminibackend.api.module.knowledgehandover.mapper.KnowledgeHandoverMapper;
import com.aluminibackend.api.module.knowledgehandover.repository.KnowledgeHandoverRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.entity.User;
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
public class KnowledgeHandoverServiceImplTest {

    @Mock
    private KnowledgeHandoverRepository handoverRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KnowledgeHandoverMapper mapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private KnowledgeHandoverServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name) {
        return User.builder()
                .userId(userId)
                .employeeId("EMP" + userId)
                .name(name)
                .email(name.toLowerCase().replace(" ", "") + "@example.com")
                .build();
    }

    private OffboardingCase createOffboardingCase(Long caseId, User employee) {
        return OffboardingCase.builder()
                .caseId(caseId)
                .employee(employee)
                .build();
    }

    private KnowledgeHandover createKnowledgeHandover(Long handoverId, OffboardingCase offboardingCase, User employee, User successor, HandoverStatus status) {
        return KnowledgeHandover.builder()
                .handoverId(handoverId)
                .offboardingCase(offboardingCase)
                .employee(employee)
                .successor(successor)
                .activeProjectsSummary("Summary")
                .keyContactsRef("Contacts")
                .systemsAccessDetails("Access")
                .criticalProcessNotes("Notes")
                .completionDeadline(LocalDate.now())
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private KnowledgeHandoverResponse createKnowledgeHandoverResponse(Long handoverId, Long caseId, Long employeeId, Long successorId, HandoverStatus status) {
        return KnowledgeHandoverResponse.builder()
                .handoverId(handoverId)
                .caseId(caseId)
                .employeeId(employeeId)
                .successorId(successorId)
                .status(status)
                .build();
    }

    private CreateKnowledgeHandoverRequest createCreateRequest(Long caseId, Long successorId) {
        CreateKnowledgeHandoverRequest request = new CreateKnowledgeHandoverRequest();
        request.setCaseId(caseId);
        request.setSuccessorId(successorId);
        request.setActiveProjectsSummary("Summary");
        request.setKeyContactsRef("Contacts");
        request.setSystemsAccessDetails("Access");
        request.setCriticalProcessNotes("Notes");
        request.setCompletionDeadline(LocalDate.now());
        return request;
    }

    private UpdateKnowledgeHandoverRequest createUpdateRequest(Long successorId, HandoverStatus status) {
        UpdateKnowledgeHandoverRequest request = new UpdateKnowledgeHandoverRequest();
        request.setSuccessorId(successorId);
        request.setActiveProjectsSummary("Updated Summary");
        request.setKeyContactsRef("Updated Contacts");
        request.setSystemsAccessDetails("Updated Access");
        request.setCriticalProcessNotes("Updated Notes");
        request.setCompletionDeadline(LocalDate.now().plusDays(5));
        request.setStatus(status);
        return request;
    }

    // --- createHandover Tests ---

    @Test
    void createHandover_Success() {
        CreateKnowledgeHandoverRequest request = createCreateRequest(20L, 2L);
        User employee = createUser(1L, "Departing Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover savedHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.existsByOffboardingCase_CaseId(20L)).thenReturn(false);
        when(offboardingCaseRepository.findById(20L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(successor));
        when(handoverRepository.save(any(KnowledgeHandover.class))).thenReturn(savedHandover);
        when(mapper.toResponse(savedHandover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.createHandover(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<KnowledgeHandover> handoverCaptor = ArgumentCaptor.forClass(KnowledgeHandover.class);
        verify(handoverRepository).save(handoverCaptor.capture());
        KnowledgeHandover captured = handoverCaptor.getValue();
        assertEquals(offboardingCase, captured.getOffboardingCase());
        assertEquals(employee, captured.getEmployee());
        assertEquals(successor, captured.getSuccessor());
        assertEquals("Summary", captured.getActiveProjectsSummary());
        assertEquals(HandoverStatus.DRAFT, captured.getStatus());

        verify(auditLogService).record("EMP1", "CREATE_KNOWLEDGE_HANDOVER", "KNOWLEDGE_HANDOVER");
        verify(handoverRepository).existsByOffboardingCase_CaseId(20L);
        verify(offboardingCaseRepository).findById(20L);
        verify(userRepository).findById(2L);
        verify(mapper).toResponse(savedHandover);
    }

    @Test
    void createHandover_ThrowsException_WhenHandoverAlreadyExists() {
        CreateKnowledgeHandoverRequest request = createCreateRequest(20L, 2L);

        when(handoverRepository.existsByOffboardingCase_CaseId(20L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createHandover(request);
        });

        assertEquals("Knowledge handover already exists for this case", exception.getMessage());
        verify(handoverRepository).existsByOffboardingCase_CaseId(20L);
        verifyNoMoreInteractions(handoverRepository);
        verifyNoInteractions(offboardingCaseRepository, userRepository, mapper, auditLogService);
    }

    @Test
    void createHandover_ThrowsException_WhenOffboardingCaseNotFound() {
        CreateKnowledgeHandoverRequest request = createCreateRequest(20L, 2L);

        when(handoverRepository.existsByOffboardingCase_CaseId(20L)).thenReturn(false);
        when(offboardingCaseRepository.findById(20L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createHandover(request);
        });

        assertEquals("Offboarding case not found", exception.getMessage());
        verify(handoverRepository).existsByOffboardingCase_CaseId(20L);
        verify(offboardingCaseRepository).findById(20L);
        verifyNoMoreInteractions(handoverRepository, offboardingCaseRepository);
        verifyNoInteractions(userRepository, mapper, auditLogService);
    }

    @Test
    void createHandover_ThrowsException_WhenSuccessorNotFound() {
        CreateKnowledgeHandoverRequest request = createCreateRequest(20L, 2L);
        User employee = createUser(1L, "Departing Employee");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        when(handoverRepository.existsByOffboardingCase_CaseId(20L)).thenReturn(false);
        when(offboardingCaseRepository.findById(20L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createHandover(request);
        });

        assertEquals("Successor not found", exception.getMessage());
        verify(handoverRepository).existsByOffboardingCase_CaseId(20L);
        verify(offboardingCaseRepository).findById(20L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(handoverRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    @Test
    void createHandover_ThrowsException_WhenSuccessorIsDepartingEmployee() {
        CreateKnowledgeHandoverRequest request = createCreateRequest(20L, 1L); // Successor is employee with ID 1
        User employee = createUser(1L, "Departing Employee");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        when(handoverRepository.existsByOffboardingCase_CaseId(20L)).thenReturn(false);
        when(offboardingCaseRepository.findById(20L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createHandover(request);
        });

        assertEquals("Successor cannot be the departing employee", exception.getMessage());
        verify(handoverRepository).existsByOffboardingCase_CaseId(20L);
        verify(offboardingCaseRepository).findById(20L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(handoverRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    // --- getHandoverById Tests ---

    @Test
    void getHandoverById_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(handover));
        when(mapper.toResponse(handover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.getHandoverById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(handoverRepository).findById(100L);
        verify(mapper).toResponse(handover);
    }

    @Test
    void getHandoverById_ThrowsException_WhenNotFound() {
        when(handoverRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getHandoverById(100L);
        });

        assertEquals("Knowledge handover not found with ID: 100", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getHandoverByCase Tests ---

    @Test
    void getHandoverByCase_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findByOffboardingCase_CaseId(20L)).thenReturn(Optional.of(handover));
        when(mapper.toResponse(handover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.getHandoverByCase(20L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(handoverRepository).findByOffboardingCase_CaseId(20L);
        verify(mapper).toResponse(handover);
    }

    @Test
    void getHandoverByCase_ThrowsException_WhenNotFound() {
        when(handoverRepository.findByOffboardingCase_CaseId(20L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getHandoverByCase(20L);
        });

        assertEquals("Knowledge handover not found for case ID: 20", exception.getMessage());
        verify(handoverRepository).findByOffboardingCase_CaseId(20L);
        verifyNoInteractions(mapper);
    }

    // --- getAllHandovers Tests ---

    @Test
    void getAllHandovers_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse response = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findAll()).thenReturn(Collections.singletonList(handover));
        when(mapper.toResponse(handover)).thenReturn(response);

        List<KnowledgeHandoverResponse> responses = service.getAllHandovers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(handoverRepository).findAll();
        verify(mapper).toResponse(handover);
    }

    // --- getHandoversByEmployee Tests ---

    @Test
    void getHandoversByEmployee_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse response = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findByEmployee_UserId(1L)).thenReturn(Collections.singletonList(handover));
        when(mapper.toResponse(handover)).thenReturn(response);

        List<KnowledgeHandoverResponse> responses = service.getHandoversByEmployee(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(handoverRepository).findByEmployee_UserId(1L);
        verify(mapper).toResponse(handover);
    }

    // --- getHandoversBySuccessor Tests ---

    @Test
    void getHandoversBySuccessor_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse response = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findBySuccessor_UserId(2L)).thenReturn(Collections.singletonList(handover));
        when(mapper.toResponse(handover)).thenReturn(response);

        List<KnowledgeHandoverResponse> responses = service.getHandoversBySuccessor(2L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(handoverRepository).findBySuccessor_UserId(2L);
        verify(mapper).toResponse(handover);
    }

    // --- getHandoversByStatus Tests ---

    @Test
    void getHandoversByStatus_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover handover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse response = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findByStatus(HandoverStatus.DRAFT)).thenReturn(Collections.singletonList(handover));
        when(mapper.toResponse(handover)).thenReturn(response);

        List<KnowledgeHandoverResponse> responses = service.getHandoversByStatus(HandoverStatus.DRAFT);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(handoverRepository).findByStatus(HandoverStatus.DRAFT);
        verify(mapper).toResponse(handover);
    }

    // --- updateHandover Tests ---

    @Test
    void updateHandover_Success_AllFieldsProvided() {
        UpdateKnowledgeHandoverRequest request = createUpdateRequest(3L, HandoverStatus.IN_PROGRESS);
        User employee = createUser(1L, "Employee");
        User oldSuccessor = createUser(2L, "Old Successor");
        User newSuccessor = createUser(3L, "New Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, oldSuccessor, HandoverStatus.DRAFT);
        KnowledgeHandover updatedHandover = createKnowledgeHandover(100L, offboardingCase, employee, newSuccessor, HandoverStatus.IN_PROGRESS);
        updatedHandover.setActiveProjectsSummary("Updated Summary");
        updatedHandover.setKeyContactsRef("Updated Contacts");
        updatedHandover.setSystemsAccessDetails("Updated Access");
        updatedHandover.setCriticalProcessNotes("Updated Notes");
        updatedHandover.setCompletionDeadline(request.getCompletionDeadline());

        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 3L, HandoverStatus.IN_PROGRESS);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newSuccessor));
        when(handoverRepository.save(existingHandover)).thenReturn(updatedHandover);
        when(mapper.toResponse(updatedHandover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.updateHandover(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<KnowledgeHandover> handoverCaptor = ArgumentCaptor.forClass(KnowledgeHandover.class);
        verify(handoverRepository).save(handoverCaptor.capture());
        KnowledgeHandover captured = handoverCaptor.getValue();
        assertEquals(newSuccessor, captured.getSuccessor());
        assertEquals("Updated Summary", captured.getActiveProjectsSummary());
        assertEquals("Updated Contacts", captured.getKeyContactsRef());
        assertEquals("Updated Access", captured.getSystemsAccessDetails());
        assertEquals("Updated Notes", captured.getCriticalProcessNotes());
        assertEquals(request.getCompletionDeadline(), captured.getCompletionDeadline());
        assertEquals(HandoverStatus.IN_PROGRESS, captured.getStatus());

        verify(auditLogService).record("EMP1", "UPDATE_KNOWLEDGE_HANDOVER", "KNOWLEDGE_HANDOVER");
        verify(handoverRepository).findById(100L);
        verify(userRepository).findById(3L);
    }

    @Test
    void updateHandover_Success_NullFieldsProvided() {
        UpdateKnowledgeHandoverRequest request = createUpdateRequest(null, null);
        request.setActiveProjectsSummary(null);
        request.setKeyContactsRef(null);
        request.setSystemsAccessDetails(null);
        request.setCriticalProcessNotes(null);
        request.setCompletionDeadline(null);

        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.DRAFT);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(handoverRepository.save(existingHandover)).thenReturn(existingHandover);
        when(mapper.toResponse(existingHandover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.updateHandover(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<KnowledgeHandover> handoverCaptor = ArgumentCaptor.forClass(KnowledgeHandover.class);
        verify(handoverRepository).save(handoverCaptor.capture());
        KnowledgeHandover captured = handoverCaptor.getValue();
        assertEquals(successor, captured.getSuccessor());
        assertEquals("Summary", captured.getActiveProjectsSummary()); // unchanged

        verify(auditLogService).record("EMP1", "UPDATE_KNOWLEDGE_HANDOVER", "KNOWLEDGE_HANDOVER");
        verify(handoverRepository).findById(100L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateHandover_ThrowsException_WhenSuccessorNotFound() {
        UpdateKnowledgeHandoverRequest request = createUpdateRequest(3L, HandoverStatus.IN_PROGRESS);
        User employee = createUser(1L, "Employee");
        User oldSuccessor = createUser(2L, "Old Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, oldSuccessor, HandoverStatus.DRAFT);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateHandover(100L, request);
        });

        assertEquals("Successor not found", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verify(userRepository).findById(3L);
        verifyNoMoreInteractions(handoverRepository, userRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    @Test
    void updateHandover_ThrowsException_WhenSuccessorIsDepartingEmployee() {
        UpdateKnowledgeHandoverRequest request = createUpdateRequest(1L, HandoverStatus.IN_PROGRESS); // Employee's own ID
        User employee = createUser(1L, "Employee");
        User oldSuccessor = createUser(2L, "Old Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);

        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, oldSuccessor, HandoverStatus.DRAFT);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateHandover(100L, request);
        });

        assertEquals("Successor cannot be the departing employee", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(handoverRepository, userRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    @Test
    void updateHandover_ThrowsException_WhenHandoverNotFound() {
        UpdateKnowledgeHandoverRequest request = createUpdateRequest(3L, HandoverStatus.IN_PROGRESS);

        when(handoverRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateHandover(100L, request);
        });

        assertEquals("Knowledge handover not found with ID: 100", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoMoreInteractions(handoverRepository);
        verifyNoInteractions(userRepository, auditLogService, mapper);
    }

    // --- markCompleted Tests ---

    @Test
    void markCompleted_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);
        KnowledgeHandover updatedHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.COMPLETED);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.COMPLETED);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(handoverRepository.save(existingHandover)).thenReturn(updatedHandover);
        when(mapper.toResponse(updatedHandover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.markCompleted(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<KnowledgeHandover> handoverCaptor = ArgumentCaptor.forClass(KnowledgeHandover.class);
        verify(handoverRepository).save(handoverCaptor.capture());
        KnowledgeHandover captured = handoverCaptor.getValue();
        assertEquals(HandoverStatus.COMPLETED, captured.getStatus());

        verify(auditLogService).record("EMP1", "COMPLETE_KNOWLEDGE_HANDOVER", "KNOWLEDGE_HANDOVER");
        verify(handoverRepository).findById(100L);
    }

    @Test
    void markCompleted_ThrowsException_WhenNotFound() {
        when(handoverRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.markCompleted(100L);
        });

        assertEquals("Knowledge handover not found with ID: 100", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoMoreInteractions(handoverRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    // --- managerSignOff Tests ---

    @Test
    void managerSignOff_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.COMPLETED);
        KnowledgeHandover updatedHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.MANAGER_SIGNED);
        KnowledgeHandoverResponse expectedResponse = createKnowledgeHandoverResponse(100L, 20L, 1L, 2L, HandoverStatus.MANAGER_SIGNED);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        when(handoverRepository.save(existingHandover)).thenReturn(updatedHandover);
        when(mapper.toResponse(updatedHandover)).thenReturn(expectedResponse);

        KnowledgeHandoverResponse response = service.managerSignOff(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<KnowledgeHandover> handoverCaptor = ArgumentCaptor.forClass(KnowledgeHandover.class);
        verify(handoverRepository).save(handoverCaptor.capture());
        KnowledgeHandover captured = handoverCaptor.getValue();
        assertEquals(HandoverStatus.MANAGER_SIGNED, captured.getStatus());

        verify(auditLogService).record("EMP2", "MANAGER_SIGNOFF_KNOWLEDGE_HANDOVER", "KNOWLEDGE_HANDOVER"); // audits successor employee ID
        verify(handoverRepository).findById(100L);
    }

    @Test
    void managerSignOff_ThrowsException_WhenStatusNotCompleted() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.IN_PROGRESS);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.managerSignOff(100L);
        });

        assertEquals("Handover must be completed before manager sign-off", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoMoreInteractions(handoverRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    @Test
    void managerSignOff_ThrowsException_WhenNotFound() {
        when(handoverRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.managerSignOff(100L);
        });

        assertEquals("Knowledge handover not found with ID: 100", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoMoreInteractions(handoverRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    // --- deleteHandover Tests ---

    @Test
    void deleteHandover_Success() {
        User employee = createUser(1L, "Employee");
        User successor = createUser(2L, "Successor");
        OffboardingCase offboardingCase = createOffboardingCase(20L, employee);
        KnowledgeHandover existingHandover = createKnowledgeHandover(100L, offboardingCase, employee, successor, HandoverStatus.DRAFT);

        when(handoverRepository.findById(100L)).thenReturn(Optional.of(existingHandover));
        doNothing().when(handoverRepository).delete(existingHandover);

        assertDoesNotThrow(() -> {
            service.deleteHandover(100L);
        });

        verify(handoverRepository).findById(100L);
        verify(handoverRepository).delete(existingHandover);
    }

    @Test
    void deleteHandover_ThrowsException_WhenNotFound() {
        when(handoverRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteHandover(100L);
        });

        assertEquals("Knowledge handover not found with ID: 100", exception.getMessage());
        verify(handoverRepository).findById(100L);
        verifyNoMoreInteractions(handoverRepository);
    }
}
