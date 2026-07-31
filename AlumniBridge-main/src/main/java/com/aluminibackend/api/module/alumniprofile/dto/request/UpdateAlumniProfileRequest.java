package com.aluminibackend.api.module.alumniprofile.dto.request;

import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import lombok.Data;

@Data
public class UpdateAlumniProfileRequest {

    private String lastRole;

    private String currentOrganisation;

    private String currentRole;

    private String linkedInRef;

    private EngagementTier engagementTier;

    private Boolean communityOptIn;

    private AlumniStatus status;
}