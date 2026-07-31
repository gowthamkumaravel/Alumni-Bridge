package com.aluminibackend.api.module.boomerangapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBoomerangApplicationRequest {

    @NotNull(message = "Alumni ID is required")
    private Long alumniId;

    @NotBlank(message = "Job requisition ID is required")
    private String jobRequisitionId;

    private Double previousTenureYears;

    @NotNull(message = "Recruiter ID is required")
    private Long recruiterId;
}