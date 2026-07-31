package com.aluminibackend.api.module.alumnireferral.dto.request;

import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateAlumniReferralRequest {

    private String candidateName;

    @Email(message = "Invalid candidate email")
    private String candidateEmail;

    private String jobRequisitionId;

    private AlumniReferralStatus status;
}