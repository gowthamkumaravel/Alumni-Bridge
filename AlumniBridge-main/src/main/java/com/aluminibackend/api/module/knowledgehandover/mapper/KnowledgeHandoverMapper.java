package com.aluminibackend.api.module.knowledgehandover.mapper;

import com.aluminibackend.api.module.knowledgehandover.dto.response.KnowledgeHandoverResponse;
import com.aluminibackend.api.module.knowledgehandover.entity.KnowledgeHandover;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeHandoverMapper {

    public KnowledgeHandoverResponse toResponse(KnowledgeHandover handover) {
        return KnowledgeHandoverResponse.builder()
                .handoverId(handover.getHandoverId())
                .caseId(handover.getOffboardingCase().getCaseId())
                .employeeId(handover.getEmployee().getUserId())
                .employeeName(handover.getEmployee().getName())
                .successorId(handover.getSuccessor().getUserId())
                .successorName(handover.getSuccessor().getName())
                .activeProjectsSummary(handover.getActiveProjectsSummary())
                .keyContactsRef(handover.getKeyContactsRef())
                .systemsAccessDetails(handover.getSystemsAccessDetails())
                .criticalProcessNotes(handover.getCriticalProcessNotes())
                .completionDeadline(handover.getCompletionDeadline())
                .status(handover.getStatus())
                .createdAt(handover.getCreatedAt())
                .updatedAt(handover.getUpdatedAt())
                .build();
    }
}