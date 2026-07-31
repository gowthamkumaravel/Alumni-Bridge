package com.aluminibackend.api.module.exitsurvey.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.exitsurvey.dto.request.SubmitExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.request.UpdateExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.response.ExitSurveyResponseDto;
import com.aluminibackend.api.module.exitsurvey.entity.ExitSurveyResponse;
import com.aluminibackend.api.module.exitsurvey.mapper.ExitSurveyMapper;
import com.aluminibackend.api.module.exitsurvey.repository.ExitSurveyResponseRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExitSurveyServiceImpl implements ExitSurveyService {

    private final ExitSurveyResponseRepository surveyRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final UserRepository userRepository;
    private final ExitSurveyMapper mapper;
    private final AuditLogService auditLogService;

    @Override
    public ExitSurveyResponseDto submitSurvey(SubmitExitSurveyRequest request) {

        if (surveyRepository.existsByOffboardingCase_CaseId(request.getCaseId())) {
            throw new RuntimeException("Exit survey already submitted for this case");
        }

        OffboardingCase offboardingCase =
                offboardingCaseRepository.findById(request.getCaseId())
                        .orElseThrow(() -> new RuntimeException("Offboarding case not found"));

        User employee =
                userRepository.findById(request.getEmployeeId())
                        .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!offboardingCase.getEmployee().getUserId().equals(employee.getUserId())) {
            throw new RuntimeException("Employee does not belong to this offboarding case");
        }

        ExitSurveyResponse survey =
                ExitSurveyResponse.builder()
                        .offboardingCase(offboardingCase)
                        .employee(employee)
                        .managerSatisfactionScore(request.getManagerSatisfactionScore())
                        .teamCultureScore(request.getTeamCultureScore())
                        .careerGrowthScore(request.getCareerGrowthScore())
                        .compensationScore(request.getCompensationScore())
                        .overallScore(request.getOverallScore())
                        .openTextFeedback(request.getOpenTextFeedback())
                        .anonymised(
                                request.getAnonymised() != null
                                        ? request.getAnonymised()
                                        : false
                        )
                        .build();

        ExitSurveyResponse savedSurvey = surveyRepository.save(survey);
        auditLogService.record(employee.getEmployeeId(), "SUBMIT_EXIT_SURVEY", "EXIT_SURVEY");

        return mapper.toResponse(savedSurvey);
    }

    @Override
    @Transactional(readOnly = true)
    public ExitSurveyResponseDto getSurveyById(Long responseId) {
        return mapper.toResponse(findSurveyById(responseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ExitSurveyResponseDto getSurveyByCase(Long caseId) {
        ExitSurveyResponse survey =
                surveyRepository.findByOffboardingCase_CaseId(caseId)
                        .orElseThrow(() -> new RuntimeException(
                                "Exit survey not found for case ID: " + caseId
                        ));

        return mapper.toResponse(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitSurveyResponseDto> getAllSurveys() {
        return surveyRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitSurveyResponseDto> getSurveysByEmployee(Long employeeId) {
        return surveyRepository.findByEmployee_UserId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitSurveyResponseDto> getAnonymousSurveys() {
        return surveyRepository.findByAnonymised(true)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ExitSurveyResponseDto updateSurvey(
            Long responseId,
            UpdateExitSurveyRequest request
    ) {
        ExitSurveyResponse survey = findSurveyById(responseId);

        if (request.getManagerSatisfactionScore() != null) {
            survey.setManagerSatisfactionScore(request.getManagerSatisfactionScore());
        }

        if (request.getTeamCultureScore() != null) {
            survey.setTeamCultureScore(request.getTeamCultureScore());
        }

        if (request.getCareerGrowthScore() != null) {
            survey.setCareerGrowthScore(request.getCareerGrowthScore());
        }

        if (request.getCompensationScore() != null) {
            survey.setCompensationScore(request.getCompensationScore());
        }

        if (request.getOverallScore() != null) {
            survey.setOverallScore(request.getOverallScore());
        }

        if (request.getOpenTextFeedback() != null) {
            survey.setOpenTextFeedback(request.getOpenTextFeedback());
        }

        if (request.getAnonymised() != null) {
            survey.setAnonymised(request.getAnonymised());
        }

        ExitSurveyResponse updatedSurvey = surveyRepository.save(survey);
        auditLogService.record(
                survey.getEmployee().getEmployeeId(),
                "UPDATE_EXIT_SURVEY",
                "EXIT_SURVEY"
        );

        return mapper.toResponse(updatedSurvey);
    }

    @Override
    public void deleteSurvey(Long responseId) {
        ExitSurveyResponse survey = findSurveyById(responseId);
        surveyRepository.delete(survey);
    }

    private ExitSurveyResponse findSurveyById(Long responseId) {
        return surveyRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException(
                        "Exit survey not found with ID: " + responseId
                ));
    }
}
