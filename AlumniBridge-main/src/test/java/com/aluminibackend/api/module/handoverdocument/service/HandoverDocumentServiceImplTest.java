package com.aluminibackend.api.module.handoverdocument.service;

import com.aluminibackend.api.module.handoverdocument.dto.request.CreateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.request.UpdateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.response.HandoverDocumentResponse;
import com.aluminibackend.api.module.handoverdocument.entity.HandoverDocument;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import com.aluminibackend.api.module.handoverdocument.mapper.HandoverDocumentMapper;
import com.aluminibackend.api.module.handoverdocument.repository.HandoverDocumentRepository;
import com.aluminibackend.api.module.knowledgehandover.entity.KnowledgeHandover;
import com.aluminibackend.api.module.knowledgehandover.repository.KnowledgeHandoverRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HandoverDocumentServiceImplTest {

    @Mock
    private HandoverDocumentRepository documentRepository;

    @Mock
    private KnowledgeHandoverRepository handoverRepository;

    @Mock
    private HandoverDocumentMapper mapper;

    @InjectMocks
    private HandoverDocumentServiceImpl service;

    // --- Helper Methods ---

    private OffboardingCase createOffboardingCase(Long caseId) {
        return OffboardingCase.builder()
                .caseId(caseId)
                .build();
    }

    private KnowledgeHandover createKnowledgeHandover(Long handoverId, OffboardingCase offboardingCase) {
        return KnowledgeHandover.builder()
                .handoverId(handoverId)
                .offboardingCase(offboardingCase)
                .build();
    }

    private HandoverDocument createHandoverDocument(Long docId, KnowledgeHandover handover, DocumentType type, String filePath, DocumentStatus status) {
        return HandoverDocument.builder()
                .docId(docId)
                .handover(handover)
                .documentType(type)
                .filePath(filePath)
                .status(status)
                .uploadedDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private HandoverDocumentResponse createHandoverDocumentResponse(Long docId, Long handoverId, Long caseId, DocumentType type, String filePath, DocumentStatus status) {
        return HandoverDocumentResponse.builder()
                .docId(docId)
                .handoverId(handoverId)
                .caseId(caseId)
                .documentType(type)
                .filePath(filePath)
                .status(status)
                .uploadedDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateHandoverDocumentRequest createUploadRequest(Long handoverId, DocumentType type, String filePath) {
        CreateHandoverDocumentRequest request = new CreateHandoverDocumentRequest();
        request.setHandoverId(handoverId);
        request.setDocumentType(type);
        request.setFilePath(filePath);
        return request;
    }

    private UpdateHandoverDocumentRequest createUpdateRequest(DocumentType type, String filePath, DocumentStatus status) {
        UpdateHandoverDocumentRequest request = new UpdateHandoverDocumentRequest();
        request.setDocumentType(type);
        request.setFilePath(filePath);
        request.setStatus(status);
        return request;
    }

    // --- uploadDocument Tests ---

    @Test
    void uploadDocument_Success() {
        CreateHandoverDocumentRequest request = createUploadRequest(10L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf");
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument savedEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse expectedResponse = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);

        when(handoverRepository.findById(10L)).thenReturn(Optional.of(handover));
        when(documentRepository.save(any(HandoverDocument.class))).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        HandoverDocumentResponse response = service.uploadDocument(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<HandoverDocument> docCaptor = ArgumentCaptor.forClass(HandoverDocument.class);
        verify(documentRepository).save(docCaptor.capture());
        HandoverDocument captured = docCaptor.getValue();
        assertEquals(handover, captured.getHandover());
        assertEquals(DocumentType.PROCESS_GUIDE, captured.getDocumentType());
        assertEquals("/files/guide.pdf", captured.getFilePath());
        assertEquals(DocumentStatus.UPLOADED, captured.getStatus());

        verify(handoverRepository).findById(10L);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void uploadDocument_ThrowsException_WhenHandoverNotFound() {
        CreateHandoverDocumentRequest request = createUploadRequest(10L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf");

        when(handoverRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.uploadDocument(request);
        });

        assertEquals("Knowledge handover not found", exception.getMessage());
        verify(handoverRepository).findById(10L);
        verifyNoInteractions(documentRepository, mapper);
    }

    // --- getDocumentById Tests ---

    @Test
    void getDocumentById_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument entity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse expectedResponse = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        HandoverDocumentResponse response = service.getDocumentById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(documentRepository).findById(100L);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getDocumentById_ThrowsException_WhenNotFound() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getDocumentById(100L);
        });

        assertEquals("Handover document not found with ID: 100", exception.getMessage());
        verify(documentRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getDocumentsByHandover Tests ---

    @Test
    void getDocumentsByHandover_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument entity1 = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocument entity2 = createHandoverDocument(101L, handover, DocumentType.PROJECT_HANDOVER_NOTE, "/files/notes.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);
        HandoverDocumentResponse response1 = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse response2 = createHandoverDocumentResponse(101L, 10L, 20L, DocumentType.PROJECT_HANDOVER_NOTE, "/files/notes.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);

        when(documentRepository.findByHandover_HandoverId(10L)).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.toResponse(entity1)).thenReturn(response1);
        when(mapper.toResponse(entity2)).thenReturn(response2);

        List<HandoverDocumentResponse> responses = service.getDocumentsByHandover(10L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(response1, responses.get(0));
        assertEquals(response2, responses.get(1));
        verify(documentRepository).findByHandover_HandoverId(10L);
        verify(mapper).toResponse(entity1);
        verify(mapper).toResponse(entity2);
    }

    @Test
    void getDocumentsByHandover_EmptyList() {
        when(documentRepository.findByHandover_HandoverId(10L)).thenReturn(Collections.emptyList());

        List<HandoverDocumentResponse> responses = service.getDocumentsByHandover(10L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(documentRepository).findByHandover_HandoverId(10L);
        verifyNoInteractions(mapper);
    }

    // --- getDocumentsByType Tests ---

    @Test
    void getDocumentsByType_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument entity = createHandoverDocument(100L, handover, DocumentType.CONTACT_LIST, "/files/contacts.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse response = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.CONTACT_LIST, "/files/contacts.pdf", DocumentStatus.UPLOADED);

        when(documentRepository.findByDocumentType(DocumentType.CONTACT_LIST)).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<HandoverDocumentResponse> responses = service.getDocumentsByType(DocumentType.CONTACT_LIST);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(documentRepository).findByDocumentType(DocumentType.CONTACT_LIST);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getDocumentsByType_EmptyList() {
        when(documentRepository.findByDocumentType(DocumentType.CONTACT_LIST)).thenReturn(Collections.emptyList());

        List<HandoverDocumentResponse> responses = service.getDocumentsByType(DocumentType.CONTACT_LIST);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(documentRepository).findByDocumentType(DocumentType.CONTACT_LIST);
        verifyNoInteractions(mapper);
    }

    // --- getDocumentsByStatus Tests ---

    @Test
    void getDocumentsByStatus_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument entity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse response = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);

        when(documentRepository.findByStatus(DocumentStatus.UPLOADED)).thenReturn(Collections.singletonList(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<HandoverDocumentResponse> responses = service.getDocumentsByStatus(DocumentStatus.UPLOADED);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(documentRepository).findByStatus(DocumentStatus.UPLOADED);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getDocumentsByStatus_EmptyList() {
        when(documentRepository.findByStatus(DocumentStatus.UPLOADED)).thenReturn(Collections.emptyList());

        List<HandoverDocumentResponse> responses = service.getDocumentsByStatus(DocumentStatus.UPLOADED);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(documentRepository).findByStatus(DocumentStatus.UPLOADED);
        verifyNoInteractions(mapper);
    }

    // --- updateDocument Tests ---

    @Test
    void updateDocument_Success_AllFieldsProvided() {
        UpdateHandoverDocumentRequest request = createUpdateRequest(DocumentType.TRAINING_NOTE, "/files/new_training.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument existingEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocument updatedEntity = createHandoverDocument(100L, handover, DocumentType.TRAINING_NOTE, "/files/new_training.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);
        HandoverDocumentResponse expectedResponse = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.TRAINING_NOTE, "/files/new_training.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(existingEntity));
        when(documentRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(expectedResponse);

        HandoverDocumentResponse response = service.updateDocument(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<HandoverDocument> docCaptor = ArgumentCaptor.forClass(HandoverDocument.class);
        verify(documentRepository).save(docCaptor.capture());
        HandoverDocument captured = docCaptor.getValue();
        assertEquals(DocumentType.TRAINING_NOTE, captured.getDocumentType());
        assertEquals("/files/new_training.pdf", captured.getFilePath());
        assertEquals(DocumentStatus.REVIEWED_BY_SUCCESSOR, captured.getStatus());

        verify(documentRepository).findById(100L);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void updateDocument_Success_NullFieldsProvided() {
        UpdateHandoverDocumentRequest request = createUpdateRequest(null, null, null);
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument existingEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocumentResponse expectedResponse = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(existingEntity));
        when(documentRepository.save(existingEntity)).thenReturn(existingEntity);
        when(mapper.toResponse(existingEntity)).thenReturn(expectedResponse);

        HandoverDocumentResponse response = service.updateDocument(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<HandoverDocument> docCaptor = ArgumentCaptor.forClass(HandoverDocument.class);
        verify(documentRepository).save(docCaptor.capture());
        HandoverDocument captured = docCaptor.getValue();
        assertEquals(DocumentType.PROCESS_GUIDE, captured.getDocumentType());
        assertEquals("/files/guide.pdf", captured.getFilePath());
        assertEquals(DocumentStatus.UPLOADED, captured.getStatus());

        verify(documentRepository).findById(100L);
        verify(mapper).toResponse(existingEntity);
    }

    @Test
    void updateDocument_ThrowsException_WhenNotFound() {
        UpdateHandoverDocumentRequest request = createUpdateRequest(DocumentType.TRAINING_NOTE, "/files/new_training.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);

        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateDocument(100L, request);
        });

        assertEquals("Handover document not found with ID: 100", exception.getMessage());
        verify(documentRepository).findById(100L);
        verifyNoMoreInteractions(documentRepository);
        verifyNoInteractions(mapper);
    }

    // --- markReviewedBySuccessor Tests ---

    @Test
    void markReviewedBySuccessor_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument existingEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);
        HandoverDocument updatedEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);
        HandoverDocumentResponse expectedResponse = createHandoverDocumentResponse(100L, 10L, 20L, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.REVIEWED_BY_SUCCESSOR);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(existingEntity));
        when(documentRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(expectedResponse);

        HandoverDocumentResponse response = service.markReviewedBySuccessor(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<HandoverDocument> docCaptor = ArgumentCaptor.forClass(HandoverDocument.class);
        verify(documentRepository).save(docCaptor.capture());
        HandoverDocument captured = docCaptor.getValue();
        assertEquals(DocumentStatus.REVIEWED_BY_SUCCESSOR, captured.getStatus());

        verify(documentRepository).findById(100L);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void markReviewedBySuccessor_ThrowsException_WhenNotFound() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.markReviewedBySuccessor(100L);
        });

        assertEquals("Handover document not found with ID: 100", exception.getMessage());
        verify(documentRepository).findById(100L);
        verifyNoMoreInteractions(documentRepository);
        verifyNoInteractions(mapper);
    }

    // --- deleteDocument Tests ---

    @Test
    void deleteDocument_Success() {
        OffboardingCase offboardingCase = createOffboardingCase(20L);
        KnowledgeHandover handover = createKnowledgeHandover(10L, offboardingCase);
        HandoverDocument existingEntity = createHandoverDocument(100L, handover, DocumentType.PROCESS_GUIDE, "/files/guide.pdf", DocumentStatus.UPLOADED);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(existingEntity));
        doNothing().when(documentRepository).delete(existingEntity);

        assertDoesNotThrow(() -> {
            service.deleteDocument(100L);
        });

        verify(documentRepository).findById(100L);
        verify(documentRepository).delete(existingEntity);
    }

    @Test
    void deleteDocument_ThrowsException_WhenNotFound() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteDocument(100L);
        });

        assertEquals("Handover document not found with ID: 100", exception.getMessage());
        verify(documentRepository).findById(100L);
        verifyNoMoreInteractions(documentRepository);
    }
}
