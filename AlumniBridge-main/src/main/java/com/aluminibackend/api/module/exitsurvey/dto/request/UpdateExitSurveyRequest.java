package com.aluminibackend.api.module.exitsurvey.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateExitSurveyRequest {

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