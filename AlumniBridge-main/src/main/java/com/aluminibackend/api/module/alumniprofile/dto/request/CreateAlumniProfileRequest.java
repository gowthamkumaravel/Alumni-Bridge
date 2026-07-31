package com.aluminibackend.api.module.alumniprofile.dto.request;

import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAlumniProfileRequest {

    @NotNull(message = "Employee ID is required")
    private String employeeId;

    private String lastRole;

    private LocalDate departureDateFrom;

    private String currentOrganisation;

    private String currentRole;

    private String linkedInRef;

    private EngagementTier engagementTier;

    private Boolean communityOptIn;
}