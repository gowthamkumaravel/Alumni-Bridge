package com.aluminibackend.api.module.alumniprofile.mapper;

import com.aluminibackend.api.module.alumniprofile.dto.response.AlumniProfileResponse;
import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import org.springframework.stereotype.Component;

@Component
public class AlumniProfileMapper {

    public AlumniProfileResponse toResponse(AlumniProfile profile) {
        return AlumniProfileResponse.builder()
                .alumniId(profile.getAlumniId())
                .employeeId(profile.getUser().getEmployeeId())
                .formerEmployeeId(profile.getFormerEmployeeId())
                .name(profile.getName())
                .email(profile.getUser().getEmail())
                .lastRole(profile.getLastRole())
                .departureDateFrom(profile.getDepartureDateFrom())
                .currentOrganisation(profile.getCurrentOrganisation())
                .currentRole(profile.getCurrentRole())
                .linkedInRef(profile.getLinkedInRef())
                .engagementTier(profile.getEngagementTier())
                .communityOptIn(profile.getCommunityOptIn())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}