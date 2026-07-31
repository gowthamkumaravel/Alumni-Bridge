package com.aluminibackend.api.module.alumniprofile.dto.response;

import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AlumniProfileResponse {

    private Long alumniId;

    private String employeeId;

    private String formerEmployeeId;

    private String name;

    private String email;

    private String lastRole;

    private LocalDate departureDateFrom;

    private String currentOrganisation;

    private String currentRole;

    private String linkedInRef;

    private EngagementTier engagementTier;

    private Boolean communityOptIn;

    private AlumniStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}