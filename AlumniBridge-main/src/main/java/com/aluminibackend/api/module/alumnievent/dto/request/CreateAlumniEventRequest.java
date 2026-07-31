package com.aluminibackend.api.module.alumnievent.dto.request;

import com.aluminibackend.api.module.alumnievent.enums.EventType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAlumniEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date cannot be in the past")
    private LocalDateTime eventDate;

    private String venue;

    private String targetCohort;
}