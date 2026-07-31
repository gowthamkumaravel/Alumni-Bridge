package com.aluminibackend.api.module.offboarding.repository;

import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OffboardingCaseRepository extends JpaRepository<OffboardingCase, Long> {

    List<OffboardingCase> findByEmployee_UserId(Long employeeId);

    List<OffboardingCase> findByHrbpOwner_UserId(Long hrbpOwnerId);

    List<OffboardingCase> findByStatus(OffboardingStatus status);

    boolean existsByEmployee_UserIdAndStatusNot(Long employeeId, OffboardingStatus status);

}