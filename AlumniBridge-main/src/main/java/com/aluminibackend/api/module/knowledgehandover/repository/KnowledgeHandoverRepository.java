package com.aluminibackend.api.module.knowledgehandover.repository;

import com.aluminibackend.api.module.knowledgehandover.entity.KnowledgeHandover;
import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeHandoverRepository
        extends JpaRepository<KnowledgeHandover, Long> {

    Optional<KnowledgeHandover> findByOffboardingCase_CaseId(Long caseId);

    boolean existsByOffboardingCase_CaseId(Long caseId);

    List<KnowledgeHandover> findByEmployee_UserId(Long employeeId);

    List<KnowledgeHandover> findBySuccessor_UserId(Long successorId);

    List<KnowledgeHandover> findByStatus(HandoverStatus status);
}