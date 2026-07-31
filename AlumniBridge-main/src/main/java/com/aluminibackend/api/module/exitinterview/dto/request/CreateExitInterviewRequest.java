package com.aluminibackend.api.module.exitinterview.dto.request;

import com.aluminibackend.api.module.exitinterview.enums.InterviewFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateExitInterviewRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Interviewer ID is required")
    private String interviewerId;

    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Scheduled date cannot be in the past")
    private LocalDateTime scheduledDate;

    @NotNull(message = "Interview format is required")
    private InterviewFormat format;
}