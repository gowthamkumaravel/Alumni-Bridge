package com.aluminibackend.api.module.exitsurvey.mapper;

import com.aluminibackend.api.module.exitsurvey.dto.response.ExitSurveyResponseDto;
import com.aluminibackend.api.module.exitsurvey.entity.ExitSurveyResponse;
import org.springframework.stereotype.Component;

@Component
public class ExitSurveyMapper {

    public ExitSurveyResponseDto toResponse(ExitSurveyResponse survey) {
        return ExitSurveyResponseDto.builder()
                .responseId(survey.getResponseId())
                .caseId(survey.getOffboardingCase().getCaseId())
                .employeeId(survey.getEmployee().getUserId())
                .employeeName(
                        Boolean.TRUE.equals(survey.getAnonymised())
                                ? "Anonymous"
                                : survey.getEmployee().getName()
                )
                .submittedDate(survey.getSubmittedDate())
                .managerSatisfactionScore(survey.getManagerSatisfactionScore())
                .teamCultureScore(survey.getTeamCultureScore())
                .careerGrowthScore(survey.getCareerGrowthScore())
                .compensationScore(survey.getCompensationScore())
                .overallScore(survey.getOverallScore())
                .openTextFeedback(survey.getOpenTextFeedback())
                .anonymised(survey.getAnonymised())
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }
}