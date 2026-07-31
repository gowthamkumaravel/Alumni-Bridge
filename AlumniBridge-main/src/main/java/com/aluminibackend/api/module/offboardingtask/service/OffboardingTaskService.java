package com.aluminibackend.api.module.offboardingtask.service;

import com.aluminibackend.api.module.offboardingtask.dto.request.*;
import com.aluminibackend.api.module.offboardingtask.dto.response.OffboardingTaskResponse;

import java.util.List;

public interface OffboardingTaskService {

    OffboardingTaskResponse createTask(
            CreateOffboardingTaskRequest request
    );

    OffboardingTaskResponse getTask(Long taskId);

    List<OffboardingTaskResponse> getTasksByCase(
            Long caseId
    );

    List<OffboardingTaskResponse> getTasksByAssignee(
            Long userId
    );

    OffboardingTaskResponse updateTask(
            Long taskId,
            UpdateOffboardingTaskRequest request
    );

    OffboardingTaskResponse markCompleted(
            Long taskId
    );

    void deleteTask(Long taskId);
}