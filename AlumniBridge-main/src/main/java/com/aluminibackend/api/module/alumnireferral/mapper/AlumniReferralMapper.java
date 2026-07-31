package com.aluminibackend.api.module.alumnireferral.mapper;

import com.aluminibackend.api.module.alumnireferral.dto.response.AlumniReferralResponse;
import com.aluminibackend.api.module.alumnireferral.entity.AlumniReferral;
import org.springframework.stereotype.Component;

@Component
public class AlumniReferralMapper {

    public AlumniReferralResponse toResponse(AlumniReferral referral) {
        return AlumniReferralResponse.builder()
                .referralId(referral.getReferralId())
                .alumniId(referral.getAlumni().getAlumniId())
                .alumniName(referral.getAlumni().getName())
                .formerEmployeeId(referral.getAlumni().getFormerEmployeeId())
                .candidateName(referral.getCandidateName())
                .candidateEmail(referral.getCandidateEmail())
                .jobRequisitionId(referral.getJobRequisitionId())
                .referralDate(referral.getReferralDate())
                .status(referral.getStatus())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}