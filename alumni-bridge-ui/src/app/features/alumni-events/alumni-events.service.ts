import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EventStatus, EventType } from '../../core/models/enums';
import {
  AlumniEventResponse,
  CreateAlumniEventRequest,
  UpdateAlumniEventRequest,
} from './alumni-event.model';

@Injectable({ providedIn: 'root' })
export class AlumniEventsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/alumni-events`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AlumniEventResponse[]> {
    return this.http.get<AlumniEventResponse[]>(this.baseUrl);
  }

  getByType(type: EventType): Observable<AlumniEventResponse[]> {
    return this.http.get<AlumniEventResponse[]>(`${this.baseUrl}/type/${type}`);
  }

  getByStatus(status: EventStatus): Observable<AlumniEventResponse[]> {
    return this.http.get<AlumniEventResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(request: CreateAlumniEventRequest): Observable<AlumniEventResponse> {
    return this.http.post<AlumniEventResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateAlumniEventRequest,
  ): Observable<AlumniEventResponse> {
    return this.http.put<AlumniEventResponse>(`${this.baseUrl}/${id}`, request);
  }

  publish(id: number): Observable<AlumniEventResponse> {
    return this.http.patch<AlumniEventResponse>(
      `${this.baseUrl}/${id}/publish`,
      {},
    );
  }

  complete(id: number): Observable<AlumniEventResponse> {
    return this.http.patch<AlumniEventResponse>(
      `${this.baseUrl}/${id}/complete`,
      {},
    );
  }

  cancel(id: number): Observable<AlumniEventResponse> {
    return this.http.patch<AlumniEventResponse>(
      `${this.baseUrl}/${id}/cancel`,
      {},
    );
  }

  rsvp(id: number): Observable<AlumniEventResponse> {
    return this.http.patch<AlumniEventResponse>(
      `${this.baseUrl}/${id}/rsvp`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
