package com.aluminibackend.api.module.alumnievent.dto.response;

import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlumniEventResponse {

    private Long eventId;

    private String title;

    private EventType eventType;

    private LocalDateTime eventDate;

    private String venue;

    private String targetCohort;

    private Integer rsvpCount;

    private EventStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}