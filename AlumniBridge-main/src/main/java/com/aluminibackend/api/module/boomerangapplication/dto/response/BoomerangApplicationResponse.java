package com.aluminibackend.api.module.boomerangapplication.dto.response;

import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BoomerangApplicationResponse {

    private Long applicationId;

    private Long alumniId;

    private String alumniName;

    private String formerEmployeeId;

    private String jobRequisitionId;

    private LocalDate applicationDate;

    private Double previousTenureYears;

    private Long recruiterId;

    private String recruiterName;

    private BoomerangApplicationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}