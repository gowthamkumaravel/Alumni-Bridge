package com.aluminibackend.api.module.rehireeligibility.dto.response;

import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RehireEligibilityResponse {

    private Long eligibilityId;

    private Long alumniId;

    private String alumniName;

    private String formerEmployeeId;

    private EligibleForRehire eligibleForRehire;

    private Integer coolingOffPeriodMonths;

    private LocalDate eligibilityReviewDate;

    private String eligibilityReason;

    private Long reviewedById;

    private String reviewedByName;

    private RehireEligibilityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}