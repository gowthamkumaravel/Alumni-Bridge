package com.aluminibackend.api.module.knowledgehandover.service;

import com.aluminibackend.api.module.knowledgehandover.dto.request.CreateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.request.UpdateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.response.KnowledgeHandoverResponse;
import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;

import java.util.List;

public interface KnowledgeHandoverService {

    KnowledgeHandoverResponse createHandover(CreateKnowledgeHandoverRequest request);

    KnowledgeHandoverResponse getHandoverById(Long handoverId);

    KnowledgeHandoverResponse getHandoverByCase(Long caseId);

    List<KnowledgeHandoverResponse> getAllHandovers();

    List<KnowledgeHandoverResponse> getHandoversByEmployee(Long employeeId);

    List<KnowledgeHandoverResponse> getHandoversBySuccessor(Long successorId);

    List<KnowledgeHandoverResponse> getHandoversByStatus(HandoverStatus status);

    KnowledgeHandoverResponse updateHandover(Long handoverId, UpdateKnowledgeHandoverRequest request);

    KnowledgeHandoverResponse markCompleted(Long handoverId);

    KnowledgeHandoverResponse managerSignOff(Long handoverId);

    void deleteHandover(Long handoverId);
}