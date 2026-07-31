package com.aluminibackend.api.module.alumnievent.dto.request;

import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateAlumniEventRequest {

    private String title;

    private EventType eventType;

    private LocalDateTime eventDate;

    private String venue;

    private String targetCohort;

    private Integer rsvpCount;

    private EventStatus status;
}