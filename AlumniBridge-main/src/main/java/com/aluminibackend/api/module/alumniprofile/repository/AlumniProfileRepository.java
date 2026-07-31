package com.aluminibackend.api.module.alumniprofile.repository;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlumniProfileRepository
        extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByUser_EmployeeId(String employeeId);

    Optional<AlumniProfile> findByFormerEmployeeId(String formerEmployeeId);

    boolean existsByUser_EmployeeId(String employeeId);

    boolean existsByFormerEmployeeId(String formerEmployeeId);

    boolean existsByAlumniIdAndUser_Email(Long alumniId, String email);

    List<AlumniProfile> findByEngagementTier(EngagementTier engagementTier);

    List<AlumniProfile> findByStatus(AlumniStatus status);

    List<AlumniProfile> findByCommunityOptInIs(Boolean communityOptIn);
}
