package com.aluminibackend.api.module.alumnievent.service;

import com.aluminibackend.api.module.alumnievent.dto.request.CreateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.request.UpdateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.response.AlumniEventResponse;
import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;

import java.util.List;

public interface AlumniEventService {

    AlumniEventResponse createEvent(CreateAlumniEventRequest request);

    AlumniEventResponse getEventById(Long eventId);

    List<AlumniEventResponse> getAllEvents();

    List<AlumniEventResponse> getEventsByType(EventType eventType);

    List<AlumniEventResponse> getEventsByStatus(EventStatus status);

    List<AlumniEventResponse> getEventsByTargetCohort(String targetCohort);

    AlumniEventResponse updateEvent(Long eventId, UpdateAlumniEventRequest request);

    AlumniEventResponse publishEvent(Long eventId);

    AlumniEventResponse completeEvent(Long eventId);

    AlumniEventResponse cancelEvent(Long eventId);

    AlumniEventResponse incrementRsvp(Long eventId);

    void deleteEvent(Long eventId);
}