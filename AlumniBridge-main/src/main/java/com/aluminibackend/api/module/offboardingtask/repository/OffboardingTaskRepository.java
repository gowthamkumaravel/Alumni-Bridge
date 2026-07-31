package com.aluminibackend.api.module.offboardingtask.repository;

import com.aluminibackend.api.module.offboardingtask.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aluminibackend.api.module.offboardingtask.entity.*;

import java.util.List;

@Repository
public interface OffboardingTaskRepository
        extends JpaRepository<OffboardingTask, Long> {

    List<OffboardingTask> findByOffboardingCase_CaseId(Long caseId);

    List<OffboardingTask> findByAssignedTo_UserId(Long userId);

    List<OffboardingTask> findByStatus(TaskStatus status);

    boolean existsByTaskIdAndAssignedTo_Email(
            Long taskId,
            String email
    );

    boolean existsByOffboardingCase_CaseIdAndAssignedTo_Email(
            Long caseId,
            String email
    );

}
