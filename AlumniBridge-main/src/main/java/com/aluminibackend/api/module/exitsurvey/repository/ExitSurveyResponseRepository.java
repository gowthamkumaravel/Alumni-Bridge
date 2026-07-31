package com.aluminibackend.api.module.exitsurvey.repository;

import com.aluminibackend.api.module.exitsurvey.entity.ExitSurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExitSurveyResponseRepository
        extends JpaRepository<ExitSurveyResponse, Long> {

    Optional<ExitSurveyResponse> findByOffboardingCase_CaseId(Long caseId);

    boolean existsByOffboardingCase_CaseId(Long caseId);

    List<ExitSurveyResponse> findByEmployee_UserId(Long employeeId);

    List<ExitSurveyResponse> findByAnonymised(Boolean anonymised);
}