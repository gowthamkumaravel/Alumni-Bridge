package com.aluminibackend.api.module.boomerangapplication.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.boomerangapplication.dto.request.CreateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.request.UpdateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.response.BoomerangApplicationResponse;
import com.aluminibackend.api.module.boomerangapplication.entity.BoomerangApplication;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import com.aluminibackend.api.module.boomerangapplication.mapper.BoomerangApplicationMapper;
import com.aluminibackend.api.module.boomerangapplication.repository.BoomerangApplicationRepository;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import com.aluminibackend.api.module.rehireeligibility.repository.RehireEligibilityRepository;
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
public class BoomerangApplicationServiceImpl
        implements BoomerangApplicationService {

    private final BoomerangApplicationRepository applicationRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final RehireEligibilityRepository rehireEligibilityRepository;
    private final UserRepository userRepository;
    private final BoomerangApplicationMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public BoomerangApplicationResponse createApplication(
            CreateBoomerangApplicationRequest request
    ) {
        if (applicationRepository.existsByAlumni_AlumniIdAndJobRequisitionId(
                request.getAlumniId(),
                request.getJobRequisitionId()
        )) {
            throw new RuntimeException(
                    "Application already exists for this alumni and job requisition"
            );
        }

        AlumniProfile alumni = alumniProfileRepository.findById(request.getAlumniId())
                .orElseThrow(() -> new RuntimeException("Alumni profile not found"));

        RehireEligibility eligibility =
                rehireEligibilityRepository.findByAlumni_AlumniId(request.getAlumniId())
                        .orElseThrow(() -> new RuntimeException(
                                "Rehire eligibility not found for this alumni"
                        ));

        if (eligibility.getStatus() != RehireEligibilityStatus.ELIGIBLE) {
            throw new RuntimeException(
                    "Alumni is not eligible for rehire. Current status: "
                            + eligibility.getStatus()
            );
        }

        User recruiter = userRepository.findById(request.getRecruiterId())
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        validateRecruiter(recruiter);

        BoomerangApplication application =
                BoomerangApplication.builder()
                        .alumni(alumni)
                        .jobRequisitionId(request.getJobRequisitionId())
                        .previousTenureYears(request.getPreviousTenureYears())
                        .recruiter(recruiter)
                        .status(BoomerangApplicationStatus.APPLIED)
                        .build();

        BoomerangApplication saved = applicationRepository.save(application);
        notifyApplication(saved, "Your boomerang application has been submitted for job " + saved.getJobRequisitionId());
        auditLogService.record(recruiter.getEmployeeId(), "CREATE_BOOMERANG_APPLICATION", "BOOMERANG_APPLICATION");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BoomerangApplicationResponse getApplicationById(
            Long applicationId
    ) {
        return mapper.toResponse(findApplicationById(applicationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoomerangApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoomerangApplicationResponse> getApplicationsByAlumni(
            Long alumniId
    ) {
        return applicationRepository.findByAlumni_AlumniId(alumniId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoomerangApplicationResponse> getApplicationsByRecruiter(
            Long recruiterId
    ) {
        return applicationRepository.findByRecruiter_UserId(recruiterId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoomerangApplicationResponse> getApplicationsByJob(
            String jobRequisitionId
    ) {
        return applicationRepository.findByJobRequisitionId(jobRequisitionId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoomerangApplicationResponse> getApplicationsByStatus(
            BoomerangApplicationStatus status
    ) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public BoomerangApplicationResponse updateApplication(
            Long applicationId,
            UpdateBoomerangApplicationRequest request
    ) {
        BoomerangApplication application = findApplicationById(applicationId);

        if (request.getJobRequisitionId() != null) {
            application.setJobRequisitionId(request.getJobRequisitionId());
        }

        if (request.getPreviousTenureYears() != null) {
            application.setPreviousTenureYears(request.getPreviousTenureYears());
        }

        if (request.getRecruiterId() != null) {
            User recruiter = userRepository.findById(request.getRecruiterId())
                    .orElseThrow(() -> new RuntimeException("Recruiter not found"));

            validateRecruiter(recruiter);
            application.setRecruiter(recruiter);
        }

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }

        BoomerangApplication saved = applicationRepository.save(application);
        notifyApplication(saved, "Your boomerang application has been updated: " + saved.getStatus());
        auditLogService.record(
                saved.getRecruiter() != null ? saved.getRecruiter().getEmployeeId() : null,
                "UPDATE_BOOMERANG_APPLICATION",
                "BOOMERANG_APPLICATION"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public BoomerangApplicationResponse updateStatus(
            Long applicationId,
            BoomerangApplicationStatus status
    ) {
        BoomerangApplication application = findApplicationById(applicationId);
        application.setStatus(status);

        BoomerangApplication saved = applicationRepository.save(application);
        notifyApplication(saved, "Your boomerang application status is now: " + saved.getStatus());
        auditLogService.record(
                saved.getRecruiter() != null ? saved.getRecruiter().getEmployeeId() : null,
                "UPDATE_BOOMERANG_STATUS",
                "BOOMERANG_APPLICATION"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public void deleteApplication(Long applicationId) {
        BoomerangApplication application = findApplicationById(applicationId);
        applicationRepository.delete(application);
    }

    private BoomerangApplication findApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException(
                        "Boomerang application not found with ID: " + applicationId
                ));
    }

    private void validateRecruiter(User recruiter) {
        if (recruiter.getRole() != UserRole.ADMIN
                && recruiter.getRole() != UserRole.TALENT_ACQUISITION
                && recruiter.getRole() != UserRole.HRBP) {
            throw new RuntimeException(
                    "Only ADMIN, TALENT_ACQUISITION or HRBP can manage boomerang applications"
            );
        }
    }

    private void notifyApplication(BoomerangApplication application, String message) {
        notificationService.createSystemNotification(
                application.getAlumni().getUser().getEmployeeId(),
                message,
                NotificationCategory.REHIRE_OPPORTUNITY
        );
    }
}
