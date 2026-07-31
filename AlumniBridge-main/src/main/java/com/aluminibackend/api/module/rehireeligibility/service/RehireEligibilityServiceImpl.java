package com.aluminibackend.api.module.rehireeligibility.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.rehireeligibility.dto.request.CreateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.request.UpdateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.response.RehireEligibilityResponse;
import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import com.aluminibackend.api.module.rehireeligibility.mapper.RehireEligibilityMapper;
import com.aluminibackend.api.module.rehireeligibility.repository.RehireEligibilityRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RehireEligibilityServiceImpl implements RehireEligibilityService {

    private final RehireEligibilityRepository eligibilityRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;
    private final RehireEligibilityMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public RehireEligibilityResponse createEligibility(
            CreateRehireEligibilityRequest request
    ) {
        if (eligibilityRepository.existsByAlumni_AlumniId(request.getAlumniId())) {
            throw new RuntimeException("Rehire eligibility already exists for this alumni");
        }

        AlumniProfile alumni = alumniProfileRepository.findById(request.getAlumniId())
                .orElseThrow(() -> new RuntimeException("Alumni profile not found"));

        User reviewedBy = userRepository.findById(request.getReviewedById())
                .orElseThrow(() -> new RuntimeException("Reviewer user not found"));

        validateReviewer(reviewedBy);

        RehireEligibility eligibility = RehireEligibility.builder()
                .alumni(alumni)
                .eligibleForRehire(request.getEligibleForRehire())
                .coolingOffPeriodMonths(request.getCoolingOffPeriodMonths())
                .eligibilityReviewDate(
                        request.getEligibilityReviewDate() != null
                                ? request.getEligibilityReviewDate()
                                : LocalDate.now()
                )
                .eligibilityReason(request.getEligibilityReason())
                .reviewedBy(reviewedBy)
                .status(
                        request.getStatus() != null
                                ? request.getStatus()
                                : resolveStatus(request.getEligibleForRehire())
                )
                .build();

        RehireEligibility saved = eligibilityRepository.save(eligibility);
        notifyEligibility(saved, "Your rehire eligibility has been reviewed: " + saved.getStatus());
        auditLogService.record(reviewedBy.getEmployeeId(), "CREATE_REHIRE_ELIGIBILITY", "REHIRE_ELIGIBILITY");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RehireEligibilityResponse getEligibilityById(Long eligibilityId) {
        return mapper.toResponse(findEligibilityById(eligibilityId));
    }

    @Override
    @Transactional(readOnly = true)
    public RehireEligibilityResponse getEligibilityByAlumni(Long alumniId) {
        RehireEligibility eligibility =
                eligibilityRepository.findByAlumni_AlumniId(alumniId)
                        .orElseThrow(() -> new RuntimeException(
                                "Rehire eligibility not found for alumni ID: " + alumniId
                        ));

        return mapper.toResponse(eligibility);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RehireEligibilityResponse> getAllEligibilities() {
        return eligibilityRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RehireEligibilityResponse> getByEligibleForRehire(
            EligibleForRehire eligibleForRehire
    ) {
        return eligibilityRepository.findByEligibleForRehire(eligibleForRehire)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RehireEligibilityResponse> getByStatus(
            RehireEligibilityStatus status
    ) {
        return eligibilityRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RehireEligibilityResponse> getByReviewer(Long reviewedById) {
        return eligibilityRepository.findByReviewedBy_UserId(reviewedById)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public RehireEligibilityResponse updateEligibility(
            Long eligibilityId,
            UpdateRehireEligibilityRequest request
    ) {
        RehireEligibility eligibility = findEligibilityById(eligibilityId);

        if (request.getReviewedById() != null) {
            User reviewer = userRepository.findById(request.getReviewedById())
                    .orElseThrow(() -> new RuntimeException("Reviewer user not found"));

            validateReviewer(reviewer);
            eligibility.setReviewedBy(reviewer);
        }

        if (request.getEligibleForRehire() != null) {
            eligibility.setEligibleForRehire(request.getEligibleForRehire());
            eligibility.setStatus(resolveStatus(request.getEligibleForRehire()));
        }

        if (request.getCoolingOffPeriodMonths() != null) {
            eligibility.setCoolingOffPeriodMonths(request.getCoolingOffPeriodMonths());
        }

        if (request.getEligibilityReviewDate() != null) {
            eligibility.setEligibilityReviewDate(request.getEligibilityReviewDate());
        }

        if (request.getEligibilityReason() != null) {
            eligibility.setEligibilityReason(request.getEligibilityReason());
        }

        if (request.getStatus() != null) {
            eligibility.setStatus(request.getStatus());
        }

        RehireEligibility saved = eligibilityRepository.save(eligibility);
        notifyEligibility(saved, "Your rehire eligibility has been updated: " + saved.getStatus());
        auditLogService.record(
                saved.getReviewedBy() != null ? saved.getReviewedBy().getEmployeeId() : null,
                "UPDATE_REHIRE_ELIGIBILITY",
                "REHIRE_ELIGIBILITY"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public RehireEligibilityResponse markEligible(Long eligibilityId) {
        RehireEligibility eligibility = findEligibilityById(eligibilityId);
        eligibility.setEligibleForRehire(EligibleForRehire.YES);
        eligibility.setStatus(RehireEligibilityStatus.ELIGIBLE);
        eligibility.setEligibilityReviewDate(LocalDate.now());

        RehireEligibility saved = eligibilityRepository.save(eligibility);
        notifyEligibility(saved, "You are eligible for rehire.");
        auditLogService.record(
                saved.getReviewedBy() != null ? saved.getReviewedBy().getEmployeeId() : null,
                "MARK_REHIRE_ELIGIBLE",
                "REHIRE_ELIGIBILITY"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public RehireEligibilityResponse markIneligible(Long eligibilityId) {
        RehireEligibility eligibility = findEligibilityById(eligibilityId);
        eligibility.setEligibleForRehire(EligibleForRehire.NO);
        eligibility.setStatus(RehireEligibilityStatus.INELIGIBLE);
        eligibility.setEligibilityReviewDate(LocalDate.now());

        RehireEligibility saved = eligibilityRepository.save(eligibility);
        notifyEligibility(saved, "Your profile is currently not eligible for rehire.");
        auditLogService.record(
                saved.getReviewedBy() != null ? saved.getReviewedBy().getEmployeeId() : null,
                "MARK_REHIRE_INELIGIBLE",
                "REHIRE_ELIGIBILITY"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public RehireEligibilityResponse markUnderReview(Long eligibilityId) {
        RehireEligibility eligibility = findEligibilityById(eligibilityId);
        eligibility.setEligibleForRehire(EligibleForRehire.CONDITIONAL);
        eligibility.setStatus(RehireEligibilityStatus.UNDER_REVIEW);
        eligibility.setEligibilityReviewDate(LocalDate.now());

        RehireEligibility saved = eligibilityRepository.save(eligibility);
        notifyEligibility(saved, "Your rehire eligibility is under review.");
        auditLogService.record(
                saved.getReviewedBy() != null ? saved.getReviewedBy().getEmployeeId() : null,
                "MARK_REHIRE_UNDER_REVIEW",
                "REHIRE_ELIGIBILITY"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public void deleteEligibility(Long eligibilityId) {
        RehireEligibility eligibility = findEligibilityById(eligibilityId);
        eligibilityRepository.delete(eligibility);
    }

    private RehireEligibility findEligibilityById(Long eligibilityId) {
        return eligibilityRepository.findById(eligibilityId)
                .orElseThrow(() -> new RuntimeException(
                        "Rehire eligibility not found with ID: " + eligibilityId
                ));
    }

    private void validateReviewer(User reviewer) {
        if (reviewer.getRole() != UserRole.ADMIN
                && reviewer.getRole() != UserRole.HRBP
                && reviewer.getRole() != UserRole.TALENT_ACQUISITION) {
            throw new RuntimeException(
                    "Only ADMIN, HRBP or TALENT_ACQUISITION can review rehire eligibility"
            );
        }
    }

    private RehireEligibilityStatus resolveStatus(
            EligibleForRehire eligibleForRehire
    ) {
        if (eligibleForRehire == EligibleForRehire.YES) {
            return RehireEligibilityStatus.ELIGIBLE;
        }

        if (eligibleForRehire == EligibleForRehire.NO) {
            return RehireEligibilityStatus.INELIGIBLE;
        }

        return RehireEligibilityStatus.UNDER_REVIEW;
    }

    private void notifyEligibility(RehireEligibility eligibility, String message) {
        notificationService.createSystemNotification(
                eligibility.getAlumni().getUser().getEmployeeId(),
                message,
                NotificationCategory.REHIRE_OPPORTUNITY
        );
    }
}
