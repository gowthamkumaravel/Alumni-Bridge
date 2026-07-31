package com.aluminibackend.api.module.clearance.repository;

import com.aluminibackend.api.module.clearance.entity.ClearanceCertificate;
import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClearanceCertificateRepository
        extends JpaRepository<ClearanceCertificate, Long> {

    Optional<ClearanceCertificate> findByOffboardingCase_CaseId(Long caseId);

    boolean existsByOffboardingCase_CaseId(Long caseId);

    List<ClearanceCertificate> findByStatus(ClearanceStatus status);
}