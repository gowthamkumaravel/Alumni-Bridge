package com.aluminibackend.api.module.exitinterview.dto.request;

import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import com.aluminibackend.api.module.exitinterview.enums.InterviewFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateExitInterviewRequest {

    private String interviewerId;

    private LocalDateTime scheduledDate;

    private InterviewFormat format;

    private String primaryReasonForLeaving;

    private String keyFeedback;

    private ExitInterviewStatus status;
}