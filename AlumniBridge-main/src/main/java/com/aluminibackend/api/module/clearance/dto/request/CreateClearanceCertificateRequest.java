package com.aluminibackend.api.module.clearance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClearanceCertificateRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Issued by ID is required")
    private Long issuedById;
}