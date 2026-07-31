package com.aluminibackend.api.module.alumnireferral.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAlumniReferralRequest {

    @NotNull(message = "Alumni ID is required")
    private Long alumniId;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @Email(message = "Invalid candidate email")
    @NotBlank(message = "Candidate email is required")
    private String candidateEmail;

    @NotBlank(message = "Job requisition ID is required")
    private String jobRequisitionId;
}