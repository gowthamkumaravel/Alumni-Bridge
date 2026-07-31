package com.aluminibackend.api.module.clearance.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.clearance.dto.request.CreateClearanceCertificateRequest;
import com.aluminibackend.api.module.clearance.dto.response.ClearanceCertificateResponse;
import com.aluminibackend.api.module.clearance.entity.ClearanceCertificate;
import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;
import com.aluminibackend.api.module.clearance.mapper.ClearanceCertificateMapper;
import com.aluminibackend.api.module.clearance.repository.ClearanceCertificateRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClearanceCertificateServiceImpl
        implements ClearanceCertificateService {

    private final ClearanceCertificateRepository certificateRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final OffboardingTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ClearanceCertificateMapper mapper;
    private final AuditLogService auditLogService;

    @Override
    public ClearanceCertificateResponse issueCertificate(
            CreateClearanceCertificateRequest request
    ) {
        if (certificateRepository.existsByOffboardingCase_CaseId(request.getCaseId())) {
            throw new RuntimeException("Clearance certificate already issued for this case");
        }

        OffboardingCase offboardingCase = offboardingCaseRepository
                .findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Offboarding case not found"));

        User issuedBy = userRepository
                .findById(request.getIssuedById())
                .orElseThrow(() -> new RuntimeException("Issued by user not found"));

        if (issuedBy.getRole() != UserRole.ADMIN && issuedBy.getRole() != UserRole.HRBP) {
            throw new RuntimeException("Only ADMIN or HRBP can issue clearance certificate");
        }

        List<OffboardingTask> tasks =
                taskRepository.findByOffboardingCase_CaseId(request.getCaseId());

        List<OffboardingTask> pendingTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED
                        && task.getStatus() != TaskStatus.WAIVED)
                .toList();

        ClearanceStatus clearanceStatus;
        String outstandingItems;

        if (pendingTasks.isEmpty()) {
            clearanceStatus = ClearanceStatus.CLEAR;
            outstandingItems = "No outstanding items";

            offboardingCase.setStatus(OffboardingStatus.COMPLETED);
        } else {
            clearanceStatus = ClearanceStatus.PENDING_ITEMS;

            outstandingItems = pendingTasks.stream()
                    .map(task -> task.getCategory()
                            + " - "
                            + task.getTaskDescription()
                            + " - "
                            + task.getStatus())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("Pending items available");

            offboardingCase.setStatus(OffboardingStatus.CLEARANCE_PENDING);
        }

        ClearanceCertificate certificate =
                ClearanceCertificate.builder()
                        .offboardingCase(offboardingCase)
                        .issuedBy(issuedBy)
                        .outstandingItems(outstandingItems)
                        .status(clearanceStatus)
                        .build();

        offboardingCaseRepository.save(offboardingCase);

        ClearanceCertificate savedCertificate =
                certificateRepository.save(certificate);
        auditLogService.record(issuedBy.getEmployeeId(), "ISSUE_CLEARANCE_CERTIFICATE", "CLEARANCE");

        return mapper.toResponse(savedCertificate);
    }

    @Override
    @Transactional(readOnly = true)
    public ClearanceCertificateResponse getCertificateById(Long certId) {
        return mapper.toResponse(findCertificateById(certId));
    }

    @Override
    @Transactional(readOnly = true)
    public ClearanceCertificateResponse getCertificateByCase(Long caseId) {
        ClearanceCertificate certificate =
                certificateRepository.findByOffboardingCase_CaseId(caseId)
                        .orElseThrow(() -> new RuntimeException(
                                "Certificate not found for case ID: " + caseId
                        ));

        return mapper.toResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClearanceCertificateResponse> getAllCertificates() {
        return certificateRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClearanceCertificateResponse> getCertificatesByStatus(
            ClearanceStatus status
    ) {
        return certificateRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void deleteCertificate(Long certId) {
        ClearanceCertificate certificate = findCertificateById(certId);
        certificateRepository.delete(certificate);
    }

    private ClearanceCertificate findCertificateById(Long certId) {
        return certificateRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException(
                        "Clearance certificate not found with ID: " + certId
                ));
    }
}
