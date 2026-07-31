package com.aluminibackend.api.module.exitinterview.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteExitInterviewRequest {

    @NotBlank(message = "Primary reason is required")
    private String primaryReasonForLeaving;

    @NotBlank(message = "Feedback is required")
    private String keyFeedback;
}