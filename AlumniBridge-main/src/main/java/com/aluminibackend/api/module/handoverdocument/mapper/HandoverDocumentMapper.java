package com.aluminibackend.api.module.handoverdocument.mapper;

import com.aluminibackend.api.module.handoverdocument.dto.response.HandoverDocumentResponse;
import com.aluminibackend.api.module.handoverdocument.entity.HandoverDocument;
import org.springframework.stereotype.Component;

@Component
public class HandoverDocumentMapper {

    public HandoverDocumentResponse toResponse(HandoverDocument document) {
        return HandoverDocumentResponse.builder()
                .docId(document.getDocId())
                .handoverId(document.getHandover().getHandoverId())
                .caseId(document.getHandover().getOffboardingCase().getCaseId())
                .documentType(document.getDocumentType())
                .filePath(document.getFilePath())
                .uploadedDate(document.getUploadedDate())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}