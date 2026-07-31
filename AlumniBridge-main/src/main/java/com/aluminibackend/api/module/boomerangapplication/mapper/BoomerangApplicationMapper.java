package com.aluminibackend.api.module.boomerangapplication.mapper;

import com.aluminibackend.api.module.boomerangapplication.dto.response.BoomerangApplicationResponse;
import com.aluminibackend.api.module.boomerangapplication.entity.BoomerangApplication;
import org.springframework.stereotype.Component;

@Component
public class BoomerangApplicationMapper {

    public BoomerangApplicationResponse toResponse(
            BoomerangApplication application
    ) {
        return BoomerangApplicationResponse.builder()
                .applicationId(application.getApplicationId())
                .alumniId(application.getAlumni().getAlumniId())
                .alumniName(application.getAlumni().getName())
                .formerEmployeeId(application.getAlumni().getFormerEmployeeId())
                .jobRequisitionId(application.getJobRequisitionId())
                .applicationDate(application.getApplicationDate())
                .previousTenureYears(application.getPreviousTenureYears())
                .recruiterId(application.getRecruiter().getUserId())
                .recruiterName(application.getRecruiter().getName())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}