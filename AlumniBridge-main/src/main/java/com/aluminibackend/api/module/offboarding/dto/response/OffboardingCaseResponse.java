package com.aluminibackend.api.module.offboarding.dto.response;

import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class OffboardingCaseResponse {

    private Long caseId;

    private Long employeeId;

    private String employeeName;

    private String employeeEmail;

    private DepartureType departureType;

    private LocalDate lastWorkingDate;

    private Long hrbpOwnerId;

    private String hrbpOwnerName;

    private OffboardingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}