package com.aluminibackend.api.module.rehireeligibility.mapper;

import com.aluminibackend.api.module.rehireeligibility.dto.response.RehireEligibilityResponse;
import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import org.springframework.stereotype.Component;

@Component
public class RehireEligibilityMapper {

    public RehireEligibilityResponse toResponse(RehireEligibility eligibility) {
        return RehireEligibilityResponse.builder()
                .eligibilityId(eligibility.getEligibilityId())
                .alumniId(eligibility.getAlumni().getAlumniId())
                .alumniName(eligibility.getAlumni().getName())
                .formerEmployeeId(eligibility.getAlumni().getFormerEmployeeId())
                .eligibleForRehire(eligibility.getEligibleForRehire())
                .coolingOffPeriodMonths(eligibility.getCoolingOffPeriodMonths())
                .eligibilityReviewDate(eligibility.getEligibilityReviewDate())
                .eligibilityReason(eligibility.getEligibilityReason())
                .reviewedById(eligibility.getReviewedBy().getUserId())
                .reviewedByName(eligibility.getReviewedBy().getName())
                .status(eligibility.getStatus())
                .createdAt(eligibility.getCreatedAt())
                .updatedAt(eligibility.getUpdatedAt())
                .build();
    }
}