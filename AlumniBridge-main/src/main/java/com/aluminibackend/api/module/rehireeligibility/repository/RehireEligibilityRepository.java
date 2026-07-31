package com.aluminibackend.api.module.rehireeligibility.repository;

import com.aluminibackend.api.module.rehireeligibility.entity.RehireEligibility;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RehireEligibilityRepository
        extends JpaRepository<RehireEligibility, Long> {

    Optional<RehireEligibility> findByAlumni_AlumniId(Long alumniId);

    boolean existsByAlumni_AlumniId(Long alumniId);

    List<RehireEligibility> findByEligibleForRehire(
            EligibleForRehire eligibleForRehire
    );

    List<RehireEligibility> findByStatus(
            RehireEligibilityStatus status
    );

    List<RehireEligibility> findByReviewedBy_UserId(Long reviewedById);
}