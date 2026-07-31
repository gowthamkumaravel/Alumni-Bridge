import { EventStatus, EventType } from '../../core/models/enums';

export interface AlumniEventResponse {
  eventId: number;
  title: string;
  eventType: EventType;
  eventDate: string;
  venue?: string;
  targetCohort?: string;
  rsvpCount?: number;
  status: EventStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAlumniEventRequest {
  title: string;
  eventType: EventType;
  eventDate: string;
  venue?: string;
  targetCohort?: string;
}

export interface UpdateAlumniEventRequest {
  title?: string;
  eventType?: EventType;
  eventDate?: string;
  venue?: string;
  targetCohort?: string;
  rsvpCount?: number;
  status?: EventStatus;
}
