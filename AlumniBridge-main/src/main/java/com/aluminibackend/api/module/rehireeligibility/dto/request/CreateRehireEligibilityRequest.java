package com.aluminibackend.api.module.rehireeligibility.dto.request;

import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRehireEligibilityRequest {

    @NotNull(message = "Alumni ID is required")
    private Long alumniId;

    @NotNull(message = "Eligible for rehire value is required")
    private EligibleForRehire eligibleForRehire;

    private Integer coolingOffPeriodMonths;

    private LocalDate eligibilityReviewDate;

    private String eligibilityReason;

    @NotNull(message = "Reviewed by ID is required")
    private Long reviewedById;

    private RehireEligibilityStatus status;
}