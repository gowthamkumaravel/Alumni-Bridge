package com.aluminibackend.api.module.knowledgehandover.dto.request;

import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateKnowledgeHandoverRequest {

    private Long successorId;

    private String activeProjectsSummary;

    private String keyContactsRef;

    private String systemsAccessDetails;

    private String criticalProcessNotes;

    private LocalDate completionDeadline;

    private HandoverStatus status;
}