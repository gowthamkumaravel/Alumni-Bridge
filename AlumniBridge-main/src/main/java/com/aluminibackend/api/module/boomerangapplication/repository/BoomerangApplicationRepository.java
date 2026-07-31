package com.aluminibackend.api.module.boomerangapplication.repository;

import com.aluminibackend.api.module.boomerangapplication.entity.BoomerangApplication;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoomerangApplicationRepository
        extends JpaRepository<BoomerangApplication, Long> {

    List<BoomerangApplication> findByAlumni_AlumniId(Long alumniId);

    List<BoomerangApplication> findByRecruiter_UserId(Long recruiterId);

    List<BoomerangApplication> findByJobRequisitionId(String jobRequisitionId);

    List<BoomerangApplication> findByStatus(BoomerangApplicationStatus status);

    boolean existsByAlumni_AlumniIdAndJobRequisitionId(
            Long alumniId,
            String jobRequisitionId
    );
}