package com.aluminibackend.api.module.handoverdocument.service;

import com.aluminibackend.api.module.handoverdocument.dto.request.CreateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.request.UpdateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.response.HandoverDocumentResponse;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;

import java.util.List;

public interface HandoverDocumentService {

    HandoverDocumentResponse uploadDocument(CreateHandoverDocumentRequest request);

    HandoverDocumentResponse getDocumentById(Long docId);

    List<HandoverDocumentResponse> getDocumentsByHandover(Long handoverId);

    List<HandoverDocumentResponse> getDocumentsByType(DocumentType documentType);

    List<HandoverDocumentResponse> getDocumentsByStatus(DocumentStatus status);

    HandoverDocumentResponse updateDocument(Long docId, UpdateHandoverDocumentRequest request);

    HandoverDocumentResponse markReviewedBySuccessor(Long docId);

    void deleteDocument(Long docId);
}