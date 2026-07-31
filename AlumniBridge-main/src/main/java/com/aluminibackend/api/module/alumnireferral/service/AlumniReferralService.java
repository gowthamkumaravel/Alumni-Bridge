package com.aluminibackend.api.module.alumnireferral.service;

import com.aluminibackend.api.module.alumnireferral.dto.request.CreateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.request.UpdateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.response.AlumniReferralResponse;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;

import java.util.List;

public interface AlumniReferralService {

    AlumniReferralResponse createReferral(CreateAlumniReferralRequest request);

    AlumniReferralResponse getReferralById(Long referralId);

    List<AlumniReferralResponse> getAllReferrals();

    List<AlumniReferralResponse> getReferralsByAlumni(Long alumniId);

    List<AlumniReferralResponse> getReferralsByCandidateEmail(String candidateEmail);

    List<AlumniReferralResponse> getReferralsByJob(String jobRequisitionId);

    List<AlumniReferralResponse> getReferralsByStatus(AlumniReferralStatus status);

    AlumniReferralResponse updateReferral(
            Long referralId,
            UpdateAlumniReferralRequest request
    );

    AlumniReferralResponse updateStatus(
            Long referralId,
            AlumniReferralStatus status
    );

    void deleteReferral(Long referralId);
}