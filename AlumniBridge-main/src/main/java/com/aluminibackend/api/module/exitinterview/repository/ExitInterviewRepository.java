package com.aluminibackend.api.module.exitinterview.repository;

import com.aluminibackend.api.module.exitinterview.entity.ExitInterview;
import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Long> {

    Optional<ExitInterview> findByOffboardingCase_CaseId(Long caseId);

    boolean existsByOffboardingCase_CaseId(Long caseId);

    List<ExitInterview> findByEmployee_EmployeeId(String employeeId);

    List<ExitInterview> findByInterviewer_EmployeeId(String interviewerId);

    List<ExitInterview> findByStatus(ExitInterviewStatus status);
}