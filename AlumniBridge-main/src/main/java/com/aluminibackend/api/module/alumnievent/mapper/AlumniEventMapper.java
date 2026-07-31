package com.aluminibackend.api.module.alumnievent.mapper;

import com.aluminibackend.api.module.alumnievent.dto.response.AlumniEventResponse;
import com.aluminibackend.api.module.alumnievent.entity.AlumniEvent;
import org.springframework.stereotype.Component;

@Component
public class AlumniEventMapper {

    public AlumniEventResponse toResponse(AlumniEvent event) {
        return AlumniEventResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .eventType(event.getEventType())
                .eventDate(event.getEventDate())
                .venue(event.getVenue())
                .targetCohort(event.getTargetCohort())
                .rsvpCount(event.getRsvpCount())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}