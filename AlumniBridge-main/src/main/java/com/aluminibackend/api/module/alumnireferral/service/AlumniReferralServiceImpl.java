package com.aluminibackend.api.module.alumnireferral.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.alumnireferral.dto.request.CreateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.request.UpdateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.response.AlumniReferralResponse;
import com.aluminibackend.api.module.alumnireferral.entity.AlumniReferral;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import com.aluminibackend.api.module.alumnireferral.mapper.AlumniReferralMapper;
import com.aluminibackend.api.module.alumnireferral.repository.AlumniReferralRepository;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniReferralServiceImpl implements AlumniReferralService {

    private final AlumniReferralRepository referralRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final AlumniReferralMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public AlumniReferralResponse createReferral(
            CreateAlumniReferralRequest request
    ) {
        if (referralRepository.existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(
                request.getAlumniId(),
                request.getCandidateEmail(),
                request.getJobRequisitionId()
        )) {
            throw new RuntimeException(
                    "This candidate is already referred by the same alumni for this job"
            );
        }

        AlumniProfile alumni = alumniProfileRepository.findById(request.getAlumniId())
                .orElseThrow(() -> new RuntimeException("Alumni profile not found"));

        AlumniReferral referral = AlumniReferral.builder()
                .alumni(alumni)
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .jobRequisitionId(request.getJobRequisitionId())
                .status(AlumniReferralStatus.SUBMITTED)
                .build();

        AlumniReferral saved = referralRepository.save(referral);
        notifyReferral(saved, "Your referral has been submitted for " + saved.getCandidateName());
        auditLogService.record(alumni.getUser().getEmployeeId(), "CREATE_ALUMNI_REFERRAL", "ALUMNI_REFERRAL");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniReferralResponse getReferralById(Long referralId) {
        return mapper.toResponse(findReferralById(referralId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReferralResponse> getAllReferrals() {
        return referralRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReferralResponse> getReferralsByAlumni(Long alumniId) {
        return referralRepository.findByAlumni_AlumniId(alumniId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReferralResponse> getReferralsByCandidateEmail(
            String candidateEmail
    ) {
        return referralRepository.findByCandidateEmail(candidateEmail)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReferralResponse> getReferralsByJob(
            String jobRequisitionId
    ) {
        return referralRepository.findByJobRequisitionId(jobRequisitionId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReferralResponse> getReferralsByStatus(
            AlumniReferralStatus status
    ) {
        return referralRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AlumniReferralResponse updateReferral(
            Long referralId,
            UpdateAlumniReferralRequest request
    ) {
        AlumniReferral referral = findReferralById(referralId);

        if (request.getCandidateName() != null) {
            referral.setCandidateName(request.getCandidateName());
        }

        if (request.getCandidateEmail() != null) {
            referral.setCandidateEmail(request.getCandidateEmail());
        }

        if (request.getJobRequisitionId() != null) {
            referral.setJobRequisitionId(request.getJobRequisitionId());
        }

        if (request.getStatus() != null) {
            referral.setStatus(request.getStatus());
        }

        AlumniReferral saved = referralRepository.save(referral);
        notifyReferral(saved, "Your referral has been updated: " + saved.getStatus());
        auditLogService.record(saved.getAlumni().getUser().getEmployeeId(), "UPDATE_ALUMNI_REFERRAL", "ALUMNI_REFERRAL");

        return mapper.toResponse(saved);
    }

    @Override
    public AlumniReferralResponse updateStatus(
            Long referralId,
            AlumniReferralStatus status
    ) {
        AlumniReferral referral = findReferralById(referralId);
        referral.setStatus(status);

        AlumniReferral saved = referralRepository.save(referral);
        notifyReferral(saved, "Your referral status is now: " + saved.getStatus());
        auditLogService.record(saved.getAlumni().getUser().getEmployeeId(), "UPDATE_ALUMNI_REFERRAL_STATUS", "ALUMNI_REFERRAL");

        return mapper.toResponse(saved);
    }

    @Override
    public void deleteReferral(Long referralId) {
        AlumniReferral referral = findReferralById(referralId);
        referralRepository.delete(referral);
    }

    private AlumniReferral findReferralById(Long referralId) {
        return referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni referral not found with ID: " + referralId
                ));
    }

    private void notifyReferral(AlumniReferral referral, String message) {
        notificationService.createSystemNotification(
                referral.getAlumni().getUser().getEmployeeId(),
                message,
                NotificationCategory.REFERRAL
        );
    }
}
