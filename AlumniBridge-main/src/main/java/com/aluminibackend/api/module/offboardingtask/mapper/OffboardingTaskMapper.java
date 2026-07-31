package com.aluminibackend.api.module.offboardingtask.mapper;

import com.aluminibackend.api.module.offboardingtask.dto.response.OffboardingTaskResponse;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import org.springframework.stereotype.Component;

@Component
public class OffboardingTaskMapper {

    public OffboardingTaskResponse toResponse(
            OffboardingTask task
    ) {

        return OffboardingTaskResponse.builder()
                .taskId(task.getTaskId())
                .caseId(task.getOffboardingCase().getCaseId())
                .category(task.getCategory())
                .taskDescription(task.getTaskDescription())
                .assignedToId(task.getAssignedTo().getUserId())
                .assignedToName(task.getAssignedTo().getName())
                .dueDate(task.getDueDate())
                .completedDate(task.getCompletedDate())
                .status(task.getStatus())
                .build();
    }
}