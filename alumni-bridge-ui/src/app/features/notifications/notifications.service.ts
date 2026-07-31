import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NotificationCategory } from '../../core/models/enums';
import {
  CreateNotificationRequest,
  NotificationResponse,
  UpdateNotificationRequest,
} from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  // Routed by the gateway to the notification-service.
  private readonly baseUrl = `${environment.apiBaseUrl}/notifications`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(`${this.baseUrl}/${id}`);
  }

  getByEmployee(employeeId: string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(
      `${this.baseUrl}/employee/${employeeId}`,
    );
  }

  getUnreadByEmployee(employeeId: string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(
      `${this.baseUrl}/employee/${employeeId}/unread`,
    );
  }

  getByCategory(
    category: NotificationCategory,
  ): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(
      `${this.baseUrl}/category/${category}`,
    );
  }

  create(
    request: CreateNotificationRequest,
  ): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateNotificationRequest,
  ): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  markRead(id: number): Observable<NotificationResponse> {
    return this.http.patch<NotificationResponse>(
      `${this.baseUrl}/${id}/read`,
      {},
    );
  }

  dismiss(id: number): Observable<NotificationResponse> {
    return this.http.patch<NotificationResponse>(
      `${this.baseUrl}/${id}/dismiss`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
