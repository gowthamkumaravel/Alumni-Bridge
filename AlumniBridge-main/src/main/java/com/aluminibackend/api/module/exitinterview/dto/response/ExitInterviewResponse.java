package com.aluminibackend.api.module.exitinterview.dto.response;

import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import com.aluminibackend.api.module.exitinterview.enums.InterviewFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExitInterviewResponse {

    private Long interviewId;

    private Long caseId;

    private String employeeId;

    private String employeeName;

    private String interviewerId;

    private String interviewerName;

    private LocalDateTime scheduledDate;

    private InterviewFormat format;

    private String primaryReasonForLeaving;

    private String keyFeedback;

    private ExitInterviewStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}