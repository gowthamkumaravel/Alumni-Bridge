package com.aluminibackend.api.module.handoverdocument.dto.request;

import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateHandoverDocumentRequest {

    @NotNull(message = "Handover ID is required")
    private Long handoverId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "File path is required")
    private String filePath;
}