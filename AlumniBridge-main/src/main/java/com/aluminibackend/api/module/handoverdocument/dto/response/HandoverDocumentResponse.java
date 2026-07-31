package com.aluminibackend.api.module.handoverdocument.dto.response;

import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HandoverDocumentResponse {

    private Long docId;

    private Long handoverId;

    private Long caseId;

    private DocumentType documentType;

    private String filePath;

    private LocalDateTime uploadedDate;

    private DocumentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}