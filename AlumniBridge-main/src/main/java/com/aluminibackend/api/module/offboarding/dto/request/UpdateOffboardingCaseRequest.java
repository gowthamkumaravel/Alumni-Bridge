package com.aluminibackend.api.module.offboarding.dto.request;

import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateOffboardingCaseRequest {

    private DepartureType departureType;

    private LocalDate lastWorkingDate;

    private Long hrbpOwnerId;

    private OffboardingStatus status;
}