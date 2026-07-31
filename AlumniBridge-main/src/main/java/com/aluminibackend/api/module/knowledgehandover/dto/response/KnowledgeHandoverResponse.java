package com.aluminibackend.api.module.knowledgehandover.dto.response;

import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class KnowledgeHandoverResponse {

    private Long handoverId;

    private Long caseId;

    private Long employeeId;

    private String employeeName;

    private Long successorId;

    private String successorName;

    private String activeProjectsSummary;

    private String keyContactsRef;

    private String systemsAccessDetails;

    private String criticalProcessNotes;

    private LocalDate completionDeadline;

    private HandoverStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}