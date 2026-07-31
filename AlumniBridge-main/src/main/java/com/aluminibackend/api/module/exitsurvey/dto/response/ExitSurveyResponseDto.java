package com.aluminibackend.api.module.exitsurvey.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExitSurveyResponseDto {

    private Long responseId;

    private Long caseId;

    private Long employeeId;

    private String employeeName;

    private LocalDateTime submittedDate;

    private Integer managerSatisfactionScore;

    private Integer teamCultureScore;

    private Integer careerGrowthScore;

    private Integer compensationScore;

    private Integer overallScore;

    private String openTextFeedback;

    private Boolean anonymised;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}