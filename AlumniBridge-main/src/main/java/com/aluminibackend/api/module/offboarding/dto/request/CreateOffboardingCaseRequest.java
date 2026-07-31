package com.aluminibackend.api.module.offboarding.dto.request;

import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOffboardingCaseRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Departure type is required")
    private DepartureType departureType;

    @NotNull(message = "Last working date is required")
    @FutureOrPresent(message = "Last working date cannot be in the past")
    private LocalDate lastWorkingDate;

    @NotNull(message = "HRBP owner ID is required")
    private Long hrbpOwnerId;
}