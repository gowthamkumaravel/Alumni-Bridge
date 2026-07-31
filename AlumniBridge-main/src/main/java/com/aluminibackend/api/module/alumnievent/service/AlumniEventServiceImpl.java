package com.aluminibackend.api.module.alumnievent.service;

import com.aluminibackend.api.module.alumnievent.dto.request.CreateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.request.UpdateAlumniEventRequest;
import com.aluminibackend.api.module.alumnievent.dto.response.AlumniEventResponse;
import com.aluminibackend.api.module.alumnievent.entity.AlumniEvent;
import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;
import com.aluminibackend.api.module.alumnievent.mapper.AlumniEventMapper;
import com.aluminibackend.api.module.alumnievent.repository.AlumniEventRepository;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniEventServiceImpl implements AlumniEventService {

    private final AlumniEventRepository eventRepository;
    private final AlumniEventMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public AlumniEventResponse createEvent(CreateAlumniEventRequest request) {
        AlumniEvent event = AlumniEvent.builder()
                .title(request.getTitle())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .venue(request.getVenue())
                .targetCohort(request.getTargetCohort())
                .rsvpCount(0)
                .status(EventStatus.PLANNED)
                .build();

        return mapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniEventResponse getEventById(Long eventId) {
        return mapper.toResponse(findEventById(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniEventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniEventResponse> getEventsByType(EventType eventType) {
        return eventRepository.findByEventType(eventType)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniEventResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniEventResponse> getEventsByTargetCohort(String targetCohort) {
        return eventRepository.findByTargetCohort(targetCohort)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AlumniEventResponse updateEvent(Long eventId, UpdateAlumniEventRequest request) {
        AlumniEvent event = findEventById(eventId);

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getVenue() != null) {
            event.setVenue(request.getVenue());
        }

        if (request.getTargetCohort() != null) {
            event.setTargetCohort(request.getTargetCohort());
        }

        if (request.getRsvpCount() != null) {
            event.setRsvpCount(request.getRsvpCount());
        }

        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        return mapper.toResponse(eventRepository.save(event));
    }

    @Override
    public AlumniEventResponse publishEvent(Long eventId) {
        AlumniEvent event = findEventById(eventId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new RuntimeException("Cancelled event cannot be published");
        }

        event.setStatus(EventStatus.PUBLISHED);
        AlumniEvent saved = eventRepository.save(event);

        notificationService.createNotificationsForAlumniCommunity(
                "New alumni event published: " + saved.getTitle(),
                NotificationCategory.ALUMNI_EVENT
        );
        auditLogService.record(null, "PUBLISH_ALUMNI_EVENT", "ALUMNI_EVENT");

        return mapper.toResponse(saved);
    }

    @Override
    public AlumniEventResponse completeEvent(Long eventId) {
        AlumniEvent event = findEventById(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new RuntimeException("Only published events can be completed");
        }

        event.setStatus(EventStatus.COMPLETED);
        AlumniEvent saved = eventRepository.save(event);
        auditLogService.record(null, "COMPLETE_ALUMNI_EVENT", "ALUMNI_EVENT");
        return mapper.toResponse(saved);
    }

    @Override
    public AlumniEventResponse cancelEvent(Long eventId) {
        AlumniEvent event = findEventById(eventId);

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new RuntimeException("Completed event cannot be cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        AlumniEvent saved = eventRepository.save(event);
        auditLogService.record(null, "CANCEL_ALUMNI_EVENT", "ALUMNI_EVENT");
        return mapper.toResponse(saved);
    }

    @Override
    public AlumniEventResponse incrementRsvp(Long eventId) {
        AlumniEvent event = findEventById(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new RuntimeException("RSVP is allowed only for published events");
        }

        event.setRsvpCount(event.getRsvpCount() + 1);
        return mapper.toResponse(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long eventId) {
        AlumniEvent event = findEventById(eventId);
        eventRepository.delete(event);
    }

    private AlumniEvent findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni event not found with ID: " + eventId
                ));
    }
}
