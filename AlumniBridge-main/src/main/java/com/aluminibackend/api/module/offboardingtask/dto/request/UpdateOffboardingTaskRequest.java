package com.aluminibackend.api.module.offboardingtask.dto.request;

import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateOffboardingTaskRequest {

    private String taskDescription;

    private Long assignedToId;

    private LocalDate dueDate;

    private TaskStatus status;
}