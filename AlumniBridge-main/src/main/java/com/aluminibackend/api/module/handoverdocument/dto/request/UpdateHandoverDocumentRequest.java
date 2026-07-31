package com.aluminibackend.api.module.handoverdocument.dto.request;

import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import lombok.Data;

@Data
public class UpdateHandoverDocumentRequest {

    private DocumentType documentType;

    private String filePath;

    private DocumentStatus status;
}