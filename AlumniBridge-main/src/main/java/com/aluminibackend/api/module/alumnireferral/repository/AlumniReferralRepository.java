package com.aluminibackend.api.module.alumnireferral.repository;

import com.aluminibackend.api.module.alumnireferral.entity.AlumniReferral;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumniReferralRepository
        extends JpaRepository<AlumniReferral, Long> {

    List<AlumniReferral> findByAlumni_AlumniId(Long alumniId);

    List<AlumniReferral> findByCandidateEmail(String candidateEmail);

    List<AlumniReferral> findByJobRequisitionId(String jobRequisitionId);

    List<AlumniReferral> findByStatus(AlumniReferralStatus status);

    boolean existsByAlumni_AlumniIdAndCandidateEmailAndJobRequisitionId(
            Long alumniId,
            String candidateEmail,
            String jobRequisitionId
    );

    boolean existsByReferralIdAndAlumni_User_Email(Long referralId, String email);

    boolean existsByAlumni_AlumniIdAndAlumni_User_Email(Long alumniId, String email);
}
