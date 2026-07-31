package com.aluminibackend.api.module.knowledgehandover.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.knowledgehandover.dto.request.CreateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.request.UpdateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.response.KnowledgeHandoverResponse;
import com.aluminibackend.api.module.knowledgehandover.entity.KnowledgeHandover;
import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import com.aluminibackend.api.module.knowledgehandover.mapper.KnowledgeHandoverMapper;
import com.aluminibackend.api.module.knowledgehandover.repository.KnowledgeHandoverRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeHandoverServiceImpl implements KnowledgeHandoverService {

    private final KnowledgeHandoverRepository handoverRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final UserRepository userRepository;
    private final KnowledgeHandoverMapper mapper;
    private final AuditLogService auditLogService;

    @Override
    public KnowledgeHandoverResponse createHandover(CreateKnowledgeHandoverRequest request) {

        if (handoverRepository.existsByOffboardingCase_CaseId(request.getCaseId())) {
            throw new RuntimeException("Knowledge handover already exists for this case");
        }

        OffboardingCase offboardingCase = offboardingCaseRepository
                .findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Offboarding case not found"));

        User successor = userRepository
                .findById(request.getSuccessorId())
                .orElseThrow(() -> new RuntimeException("Successor not found"));

        if (offboardingCase.getEmployee().getUserId().equals(successor.getUserId())) {
            throw new RuntimeException("Successor cannot be the departing employee");
        }

        KnowledgeHandover handover = KnowledgeHandover.builder()
                .offboardingCase(offboardingCase)
                .employee(offboardingCase.getEmployee())
                .successor(successor)
                .activeProjectsSummary(request.getActiveProjectsSummary())
                .keyContactsRef(request.getKeyContactsRef())
                .systemsAccessDetails(request.getSystemsAccessDetails())
                .criticalProcessNotes(request.getCriticalProcessNotes())
                .completionDeadline(request.getCompletionDeadline())
                .status(HandoverStatus.DRAFT)
                .build();

        KnowledgeHandover savedHandover = handoverRepository.save(handover);
        auditLogService.record(
                offboardingCase.getEmployee().getEmployeeId(),
                "CREATE_KNOWLEDGE_HANDOVER",
                "KNOWLEDGE_HANDOVER"
        );

        return mapper.toResponse(savedHandover);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeHandoverResponse getHandoverById(Long handoverId) {
        return mapper.toResponse(findHandoverById(handoverId));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeHandoverResponse getHandoverByCase(Long caseId) {
        KnowledgeHandover handover =
                handoverRepository.findByOffboardingCase_CaseId(caseId)
                        .orElseThrow(() -> new RuntimeException(
                                "Knowledge handover not found for case ID: " + caseId
                        ));

        return mapper.toResponse(handover);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeHandoverResponse> getAllHandovers() {
        return handoverRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeHandoverResponse> getHandoversByEmployee(Long employeeId) {
        return handoverRepository.findByEmployee_UserId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeHandoverResponse> getHandoversBySuccessor(Long successorId) {
        return handoverRepository.findBySuccessor_UserId(successorId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeHandoverResponse> getHandoversByStatus(HandoverStatus status) {
        return handoverRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public KnowledgeHandoverResponse updateHandover(
            Long handoverId,
            UpdateKnowledgeHandoverRequest request
    ) {
        KnowledgeHandover handover = findHandoverById(handoverId);

        if (request.getSuccessorId() != null) {
            User successor = userRepository.findById(request.getSuccessorId())
                    .orElseThrow(() -> new RuntimeException("Successor not found"));

            if (handover.getEmployee().getUserId().equals(successor.getUserId())) {
                throw new RuntimeException("Successor cannot be the departing employee");
            }

            handover.setSuccessor(successor);
        }

        if (request.getActiveProjectsSummary() != null) {
            handover.setActiveProjectsSummary(request.getActiveProjectsSummary());
        }

        if (request.getKeyContactsRef() != null) {
            handover.setKeyContactsRef(request.getKeyContactsRef());
        }

        if (request.getSystemsAccessDetails() != null) {
            handover.setSystemsAccessDetails(request.getSystemsAccessDetails());
        }

        if (request.getCriticalProcessNotes() != null) {
            handover.setCriticalProcessNotes(request.getCriticalProcessNotes());
        }

        if (request.getCompletionDeadline() != null) {
            handover.setCompletionDeadline(request.getCompletionDeadline());
        }

        if (request.getStatus() != null) {
            handover.setStatus(request.getStatus());
        }

        KnowledgeHandover updatedHandover = handoverRepository.save(handover);
        auditLogService.record(
                updatedHandover.getEmployee().getEmployeeId(),
                "UPDATE_KNOWLEDGE_HANDOVER",
                "KNOWLEDGE_HANDOVER"
        );

        return mapper.toResponse(updatedHandover);
    }

    @Override
    public KnowledgeHandoverResponse markCompleted(Long handoverId) {
        KnowledgeHandover handover = findHandoverById(handoverId);
        handover.setStatus(HandoverStatus.COMPLETED);

        KnowledgeHandover updatedHandover = handoverRepository.save(handover);
        auditLogService.record(
                updatedHandover.getEmployee().getEmployeeId(),
                "COMPLETE_KNOWLEDGE_HANDOVER",
                "KNOWLEDGE_HANDOVER"
        );

        return mapper.toResponse(updatedHandover);
    }

    @Override
    public KnowledgeHandoverResponse managerSignOff(Long handoverId) {
        KnowledgeHandover handover = findHandoverById(handoverId);

        if (handover.getStatus() != HandoverStatus.COMPLETED) {
            throw new RuntimeException("Handover must be completed before manager sign-off");
        }

        handover.setStatus(HandoverStatus.MANAGER_SIGNED);

        KnowledgeHandover updatedHandover = handoverRepository.save(handover);
        auditLogService.record(
                updatedHandover.getSuccessor().getEmployeeId(),
                "MANAGER_SIGNOFF_KNOWLEDGE_HANDOVER",
                "KNOWLEDGE_HANDOVER"
        );

        return mapper.toResponse(updatedHandover);
    }

    @Override
    public void deleteHandover(Long handoverId) {
        KnowledgeHandover handover = findHandoverById(handoverId);
        handoverRepository.delete(handover);
    }

    private KnowledgeHandover findHandoverById(Long handoverId) {
        return handoverRepository.findById(handoverId)
                .orElseThrow(() -> new RuntimeException(
                        "Knowledge handover not found with ID: " + handoverId
                ));
    }
}
