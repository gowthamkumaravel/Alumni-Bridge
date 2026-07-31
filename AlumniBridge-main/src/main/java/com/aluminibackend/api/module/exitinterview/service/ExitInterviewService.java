package com.aluminibackend.api.module.exitinterview.service;

import com.aluminibackend.api.module.exitinterview.dto.request.CreateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.request.UpdateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.response.ExitInterviewResponse;
import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;

import java.util.List;

public interface ExitInterviewService {

    ExitInterviewResponse scheduleInterview(CreateExitInterviewRequest request);

    ExitInterviewResponse getInterviewById(Long interviewId);

    ExitInterviewResponse getInterviewByCase(Long caseId);

    List<ExitInterviewResponse> getAllInterviews();

    List<ExitInterviewResponse> getInterviewsByEmployee(String employeeId);

    List<ExitInterviewResponse> getInterviewsByInterviewer(String interviewerId);

    List<ExitInterviewResponse> getInterviewsByStatus(ExitInterviewStatus status);

    ExitInterviewResponse updateInterview(Long interviewId, UpdateExitInterviewRequest request);

    ExitInterviewResponse completeInterview(Long interviewId, String primaryReason, String feedback);

    void deleteInterview(Long interviewId);
}