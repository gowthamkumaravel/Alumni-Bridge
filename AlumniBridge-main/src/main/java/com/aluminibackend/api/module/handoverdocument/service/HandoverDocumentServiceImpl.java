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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HandoverDocumentServiceImpl implements HandoverDocumentService {

    private final HandoverDocumentRepository documentRepository;
    private final KnowledgeHandoverRepository handoverRepository;
    private final HandoverDocumentMapper mapper;

    @Override
    public HandoverDocumentResponse uploadDocument(
            CreateHandoverDocumentRequest request
    ) {
        KnowledgeHandover handover =
                handoverRepository.findById(request.getHandoverId())
                        .orElseThrow(() -> new RuntimeException("Knowledge handover not found"));

        HandoverDocument document =
                HandoverDocument.builder()
                        .handover(handover)
                        .documentType(request.getDocumentType())
                        .filePath(request.getFilePath())
                        .status(DocumentStatus.UPLOADED)
                        .build();

        HandoverDocument savedDocument =
                documentRepository.save(document);

        return mapper.toResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverDocumentResponse getDocumentById(Long docId) {
        return mapper.toResponse(findDocumentById(docId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverDocumentResponse> getDocumentsByHandover(Long handoverId) {
        return documentRepository.findByHandover_HandoverId(handoverId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverDocumentResponse> getDocumentsByType(
            DocumentType documentType
    ) {
        return documentRepository.findByDocumentType(documentType)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverDocumentResponse> getDocumentsByStatus(
            DocumentStatus status
    ) {
        return documentRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public HandoverDocumentResponse updateDocument(
            Long docId,
            UpdateHandoverDocumentRequest request
    ) {
        HandoverDocument document = findDocumentById(docId);

        if (request.getDocumentType() != null) {
            document.setDocumentType(request.getDocumentType());
        }

        if (request.getFilePath() != null) {
            document.setFilePath(request.getFilePath());
        }

        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }

        HandoverDocument updatedDocument =
                documentRepository.save(document);

        return mapper.toResponse(updatedDocument);
    }

    @Override
    public HandoverDocumentResponse markReviewedBySuccessor(Long docId) {
        HandoverDocument document = findDocumentById(docId);
        document.setStatus(DocumentStatus.REVIEWED_BY_SUCCESSOR);

        HandoverDocument updatedDocument =
                documentRepository.save(document);

        return mapper.toResponse(updatedDocument);
    }

    @Override
    public void deleteDocument(Long docId) {
        HandoverDocument document = findDocumentById(docId);
        documentRepository.delete(document);
    }

    private HandoverDocument findDocumentById(Long docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException(
                        "Handover document not found with ID: " + docId
                ));
    }
}