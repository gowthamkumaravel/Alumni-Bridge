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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlumniEventServiceImplTest {

    @Mock
    private AlumniEventRepository eventRepository;

    @Mock
    private AlumniEventMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AlumniEventServiceImpl service;

    // --- Helper Methods ---

    private AlumniEvent createAlumniEvent(Long eventId, String title, EventType type, LocalDateTime date, String venue, String cohort, Integer rsvp, EventStatus status) {
        return AlumniEvent.builder()
                .eventId(eventId)
                .title(title)
                .eventType(type)
                .eventDate(date)
                .venue(venue)
                .targetCohort(cohort)
                .rsvpCount(rsvp)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AlumniEventResponse createAlumniEventResponse(Long eventId, String title, EventType type, LocalDateTime date, String venue, String cohort, Integer rsvp, EventStatus status) {
        return AlumniEventResponse.builder()
                .eventId(eventId)
                .title(title)
                .eventType(type)
                .eventDate(date)
                .venue(venue)
                .targetCohort(cohort)
                .rsvpCount(rsvp)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateAlumniEventRequest createCreateRequest(String title, EventType type, LocalDateTime date, String venue, String cohort) {
        CreateAlumniEventRequest request = new CreateAlumniEventRequest();
        request.setTitle(title);
        request.setEventType(type);
        request.setEventDate(date);
        request.setVenue(venue);
        request.setTargetCohort(cohort);
        return request;
    }

    private UpdateAlumniEventRequest createUpdateRequest(String title, EventType type, LocalDateTime date, String venue, String cohort, Integer rsvp, EventStatus status) {
        UpdateAlumniEventRequest request = new UpdateAlumniEventRequest();
        request.setTitle(title);
        request.setEventType(type);
        request.setEventDate(date);
        request.setVenue(venue);
        request.setTargetCohort(cohort);
        request.setRsvpCount(rsvp);
        request.setStatus(status);
        return request;
    }

    // --- createEvent Tests ---

    @Test
    void createEvent_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        CreateAlumniEventRequest request = createCreateRequest("Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024");
        AlumniEvent savedEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.save(any(AlumniEvent.class))).thenReturn(savedEvent);
        when(mapper.toResponse(savedEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.createEvent(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals("Annual Reunion", captured.getTitle());
        assertEquals(EventType.REUNION, captured.getEventType());
        assertEquals(date, captured.getEventDate());
        assertEquals("Hotel Grand", captured.getVenue());
        assertEquals("2020-2024", captured.getTargetCohort());
        assertEquals(0, captured.getRsvpCount());
        assertEquals(EventStatus.PLANNED, captured.getStatus());

        verify(mapper).toResponse(savedEvent);
    }

    // --- getEventById Tests ---

    @Test
    void getEventById_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent event = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(mapper.toResponse(event)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.getEventById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(eventRepository).findById(100L);
        verify(mapper).toResponse(event);
    }

    @Test
    void getEventById_ThrowsException_WhenNotFound() {
        when(eventRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getEventById(100L);
        });

        assertEquals("Alumni event not found with ID: 100", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getAllEvents Tests ---

    @Test
    void getAllEvents_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent event = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse response = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findAll()).thenReturn(Collections.singletonList(event));
        when(mapper.toResponse(event)).thenReturn(response);

        List<AlumniEventResponse> responses = service.getAllEvents();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(eventRepository).findAll();
        verify(mapper).toResponse(event);
    }

    // --- getEventsByType Tests ---

    @Test
    void getEventsByType_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent event = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse response = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findByEventType(EventType.REUNION)).thenReturn(Collections.singletonList(event));
        when(mapper.toResponse(event)).thenReturn(response);

        List<AlumniEventResponse> responses = service.getEventsByType(EventType.REUNION);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(eventRepository).findByEventType(EventType.REUNION);
        verify(mapper).toResponse(event);
    }

    // --- getEventsByStatus Tests ---

    @Test
    void getEventsByStatus_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent event = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse response = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findByStatus(EventStatus.PLANNED)).thenReturn(Collections.singletonList(event));
        when(mapper.toResponse(event)).thenReturn(response);

        List<AlumniEventResponse> responses = service.getEventsByStatus(EventStatus.PLANNED);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(eventRepository).findByStatus(EventStatus.PLANNED);
        verify(mapper).toResponse(event);
    }

    // --- getEventsByTargetCohort Tests ---

    @Test
    void getEventsByTargetCohort_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent event = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse response = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findByTargetCohort("2020-2024")).thenReturn(Collections.singletonList(event));
        when(mapper.toResponse(event)).thenReturn(response);

        List<AlumniEventResponse> responses = service.getEventsByTargetCohort("2020-2024");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(eventRepository).findByTargetCohort("2020-2024");
        verify(mapper).toResponse(event);
    }

    // --- updateEvent Tests ---

    @Test
    void updateEvent_Success_AllFieldsProvided() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(5);
        UpdateAlumniEventRequest request = createUpdateRequest("New Title", EventType.NETWORKING, newDate, "Zoom", "2025", 10, EventStatus.PUBLISHED);
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEvent updatedEvent = createAlumniEvent(100L, "New Title", EventType.NETWORKING, newDate, "Zoom", "2025", 10, EventStatus.PUBLISHED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "New Title", EventType.NETWORKING, newDate, "Zoom", "2025", 10, EventStatus.PUBLISHED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(updatedEvent);
        when(mapper.toResponse(updatedEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.updateEvent(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals("New Title", captured.getTitle());
        assertEquals(EventType.NETWORKING, captured.getEventType());
        assertEquals(newDate, captured.getEventDate());
        assertEquals("Zoom", captured.getVenue());
        assertEquals("2025", captured.getTargetCohort());
        assertEquals(10, captured.getRsvpCount());
        assertEquals(EventStatus.PUBLISHED, captured.getStatus());

        verify(eventRepository).findById(100L);
        verify(mapper).toResponse(updatedEvent);
    }

    @Test
    void updateEvent_Success_NullFieldsProvided() {
        UpdateAlumniEventRequest request = createUpdateRequest(null, null, null, null, null, null, null);
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);
        when(mapper.toResponse(existingEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.updateEvent(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals("Annual Reunion", captured.getTitle());
        assertEquals(EventType.REUNION, captured.getEventType());
        assertEquals(date, captured.getEventDate());
        assertEquals("Hotel Grand", captured.getVenue());
        assertEquals("2020-2024", captured.getTargetCohort());
        assertEquals(0, captured.getRsvpCount());
        assertEquals(EventStatus.PLANNED, captured.getStatus());

        verify(eventRepository).findById(100L);
        verify(mapper).toResponse(existingEvent);
    }

    @Test
    void updateEvent_ThrowsException_WhenNotFound() {
        UpdateAlumniEventRequest request = createUpdateRequest("New Title", EventType.NETWORKING, LocalDateTime.now(), "Zoom", "2025", 10, EventStatus.PUBLISHED);

        when(eventRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateEvent(100L, request);
        });

        assertEquals("Alumni event not found with ID: 100", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(mapper);
    }

    // --- publishEvent Tests ---

    @Test
    void publishEvent_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);
        AlumniEvent publishedEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PUBLISHED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PUBLISHED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(publishedEvent);
        when(mapper.toResponse(publishedEvent)).thenReturn(expectedResponse);
        doNothing().when(notificationService).createNotificationsForAlumniCommunity(
                "New alumni event published: Annual Reunion",
                NotificationCategory.ALUMNI_EVENT
        );

        AlumniEventResponse response = service.publishEvent(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals(EventStatus.PUBLISHED, captured.getStatus());

        verify(notificationService).createNotificationsForAlumniCommunity(
                "New alumni event published: Annual Reunion",
                NotificationCategory.ALUMNI_EVENT
        );
        verify(auditLogService).record(null, "PUBLISH_ALUMNI_EVENT", "ALUMNI_EVENT");
        verify(eventRepository).findById(100L);
        verify(mapper).toResponse(publishedEvent);
    }

    @Test
    void publishEvent_ThrowsException_WhenCancelled() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.CANCELLED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.publishEvent(100L);
        });

        assertEquals("Cancelled event cannot be published", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(notificationService, auditLogService, mapper);
    }

    // --- completeEvent Tests ---

    @Test
    void completeEvent_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PUBLISHED);
        AlumniEvent completedEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.COMPLETED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.COMPLETED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(completedEvent);
        when(mapper.toResponse(completedEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.completeEvent(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals(EventStatus.COMPLETED, captured.getStatus());

        verify(auditLogService).record(null, "COMPLETE_ALUMNI_EVENT", "ALUMNI_EVENT");
        verify(eventRepository).findById(100L);
    }

    @Test
    void completeEvent_ThrowsException_WhenNotPublished() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.completeEvent(100L);
        });

        assertEquals("Only published events can be completed", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    // --- cancelEvent Tests ---

    @Test
    void cancelEvent_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PUBLISHED);
        AlumniEvent cancelledEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.CANCELLED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.CANCELLED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(cancelledEvent);
        when(mapper.toResponse(cancelledEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.cancelEvent(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals(EventStatus.CANCELLED, captured.getStatus());

        verify(auditLogService).record(null, "CANCEL_ALUMNI_EVENT", "ALUMNI_EVENT");
        verify(eventRepository).findById(100L);
    }

    @Test
    void cancelEvent_ThrowsException_WhenCompleted() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.COMPLETED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.cancelEvent(100L);
        });

        assertEquals("Completed event cannot be cancelled", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(auditLogService, mapper);
    }

    // --- incrementRsvp Tests ---

    @Test
    void incrementRsvp_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 5, EventStatus.PUBLISHED);
        AlumniEvent updatedEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 6, EventStatus.PUBLISHED);
        AlumniEventResponse expectedResponse = createAlumniEventResponse(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 6, EventStatus.PUBLISHED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(updatedEvent);
        when(mapper.toResponse(updatedEvent)).thenReturn(expectedResponse);

        AlumniEventResponse response = service.incrementRsvp(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniEvent> eventCaptor = ArgumentCaptor.forClass(AlumniEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        AlumniEvent captured = eventCaptor.getValue();
        assertEquals(6, captured.getRsvpCount());

        verify(eventRepository).findById(100L);
    }

    @Test
    void incrementRsvp_ThrowsException_WhenNotPublished() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.incrementRsvp(100L);
        });

        assertEquals("RSVP is allowed only for published events", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(mapper);
    }

    // --- deleteEvent Tests ---

    @Test
    void deleteEvent_Success() {
        LocalDateTime date = LocalDateTime.now().plusDays(2);
        AlumniEvent existingEvent = createAlumniEvent(100L, "Annual Reunion", EventType.REUNION, date, "Hotel Grand", "2020-2024", 0, EventStatus.PLANNED);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(existingEvent));
        doNothing().when(eventRepository).delete(existingEvent);

        assertDoesNotThrow(() -> {
            service.deleteEvent(100L);
        });

        verify(eventRepository).findById(100L);
        verify(eventRepository).delete(existingEvent);
    }

    @Test
    void deleteEvent_ThrowsException_WhenNotFound() {
        when(eventRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteEvent(100L);
        });

        assertEquals("Alumni event not found with ID: 100", exception.getMessage());
        verify(eventRepository).findById(100L);
        verifyNoMoreInteractions(eventRepository);
    }
}
