package com.aluminibackend.api.module.knowledgehandover.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateKnowledgeHandoverRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Successor ID is required")
    private Long successorId;

    private String activeProjectsSummary;

    private String keyContactsRef;

    private String systemsAccessDetails;

    private String criticalProcessNotes;

    @FutureOrPresent(message = "Completion deadline cannot be in the past")
    private LocalDate completionDeadline;
}