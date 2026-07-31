package com.aluminibackend.api.module.exitinterview.mapper;

import com.aluminibackend.api.module.exitinterview.dto.response.ExitInterviewResponse;
import com.aluminibackend.api.module.exitinterview.entity.ExitInterview;
import org.springframework.stereotype.Component;

@Component
public class ExitInterviewMapper {

    public ExitInterviewResponse toResponse(ExitInterview interview) {
        return ExitInterviewResponse.builder()
                .interviewId(interview.getInterviewId())
                .caseId(interview.getOffboardingCase().getCaseId())
                .employeeId(interview.getEmployee().getEmployeeId())
                .employeeName(interview.getEmployee().getName())
                .interviewerId(interview.getInterviewer().getEmployeeId())
                .interviewerName(interview.getInterviewer().getName())
                .scheduledDate(interview.getScheduledDate())
                .format(interview.getFormat())
                .primaryReasonForLeaving(interview.getPrimaryReasonForLeaving())
                .keyFeedback(interview.getKeyFeedback())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}