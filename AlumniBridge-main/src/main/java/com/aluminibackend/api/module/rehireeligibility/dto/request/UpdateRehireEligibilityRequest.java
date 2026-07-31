package com.aluminibackend.api.module.rehireeligibility.dto.request;

import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateRehireEligibilityRequest {

    private EligibleForRehire eligibleForRehire;

    private Integer coolingOffPeriodMonths;

    private LocalDate eligibilityReviewDate;

    private String eligibilityReason;

    private Long reviewedById;

    private RehireEligibilityStatus status;
}