package com.aluminibackend.api.module.alumnireferral.dto.response;

import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AlumniReferralResponse {

    private Long referralId;

    private Long alumniId;

    private String alumniName;

    private String formerEmployeeId;

    private String candidateName;

    private String candidateEmail;

    private String jobRequisitionId;

    private LocalDate referralDate;

    private AlumniReferralStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}