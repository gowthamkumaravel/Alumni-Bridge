package com.aluminibackend.api.module.alumnievent.controller;

import com.aluminibackend.api.module.alumnievent.dto.request.CreateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.request.UpdateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.response.AlumniEventResponse;
import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;
import com.aluminibackend.api.module.alumnievent.service.AlumniEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni-events")
@RequiredArgsConstructor
public class AlumniEventController {

    private final AlumniEventService eventService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @PostMapping
    public ResponseEntity<AlumniEventResponse> createEvent(
            @Valid @RequestBody CreateAlumniEventRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS', 'EMPLOYEE', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<AlumniEventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS', 'EMPLOYEE', 'TALENT_ACQUISITION')")
    @GetMapping("/{eventId}")
    public ResponseEntity<AlumniEventResponse> getEventById(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<AlumniEventResponse>> getEventsByType(
            @PathVariable EventType eventType
    ) {
        return ResponseEntity.ok(eventService.getEventsByType(eventType));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlumniEventResponse>> getEventsByStatus(
            @PathVariable EventStatus status
    ) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @GetMapping("/cohort/{targetCohort}")
    public ResponseEntity<List<AlumniEventResponse>> getEventsByTargetCohort(
            @PathVariable String targetCohort
    ) {
        return ResponseEntity.ok(eventService.getEventsByTargetCohort(targetCohort));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @PutMapping("/{eventId}")
    public ResponseEntity<AlumniEventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateAlumniEventRequest request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<AlumniEventResponse> publishEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.publishEvent(eventId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @PatchMapping("/{eventId}/complete")
    public ResponseEntity<AlumniEventResponse> completeEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.completeEvent(eventId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS')")
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<AlumniEventResponse> cancelEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.cancelEvent(eventId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNI_RELATIONS', 'EMPLOYEE')")
    @PatchMapping("/{eventId}/rsvp")
    public ResponseEntity<AlumniEventResponse> incrementRsvp(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.incrementRsvp(eventId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId
    ) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}