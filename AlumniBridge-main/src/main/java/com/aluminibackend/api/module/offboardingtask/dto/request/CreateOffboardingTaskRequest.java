package com.aluminibackend.api.module.offboardingtask.dto.request;

import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOffboardingTaskRequest {

    @NotNull
    private Long caseId;

    @NotNull
    private TaskCategory category;

    @NotBlank
    private String taskDescription;

    @NotNull
    private Long assignedToId;

    @NotNull
    private LocalDate dueDate;
}