package com.aluminibackend.api.module.exitsurvey.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitExitSurveyRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Min(1)
    @Max(5)
    private Integer managerSatisfactionScore;

    @Min(1)
    @Max(5)
    private Integer teamCultureScore;

    @Min(1)
    @Max(5)
    private Integer careerGrowthScore;

    @Min(1)
    @Max(5)
    private Integer compensationScore;

    @Min(1)
    @Max(5)
    private Integer overallScore;

    private String openTextFeedback;

    private Boolean anonymised;
}