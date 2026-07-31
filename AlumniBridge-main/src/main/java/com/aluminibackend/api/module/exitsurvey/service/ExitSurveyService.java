package com.aluminibackend.api.module.exitsurvey.service;

import com.aluminibackend.api.module.exitsurvey.dto.request.SubmitExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.request.UpdateExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.response.ExitSurveyResponseDto;

import java.util.List;

public interface ExitSurveyService {

    ExitSurveyResponseDto submitSurvey(SubmitExitSurveyRequest request);

    ExitSurveyResponseDto getSurveyById(Long responseId);

    ExitSurveyResponseDto getSurveyByCase(Long caseId);

    List<ExitSurveyResponseDto> getAllSurveys();

    List<ExitSurveyResponseDto> getSurveysByEmployee(Long employeeId);

    List<ExitSurveyResponseDto> getAnonymousSurveys();

    ExitSurveyResponseDto updateSurvey(Long responseId, UpdateExitSurveyRequest request);

    void deleteSurvey(Long responseId);
}