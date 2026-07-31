package com.aluminibackend.api.module.offboardingtask.dto.response;

import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class OffboardingTaskResponse {

    private Long taskId;

    private Long caseId;

    private TaskCategory category;

    private String taskDescription;

    private Long assignedToId;

    private String assignedToName;

    private LocalDate dueDate;

    private LocalDate completedDate;

    private TaskStatus status;
}