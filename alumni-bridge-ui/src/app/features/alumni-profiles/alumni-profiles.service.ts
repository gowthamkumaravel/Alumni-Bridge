import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AlumniStatus, EngagementTier } from '../../core/models/enums';
import {
  AlumniProfileResponse,
  CreateAlumniProfileRequest,
  UpdateAlumniProfileRequest,
} from './alumni-profile.model';

@Injectable({ providedIn: 'root' })
export class AlumniProfilesService {
  private readonly baseUrl = `${environment.apiBaseUrl}/alumni-profiles`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AlumniProfileResponse[]> {
    return this.http.get<AlumniProfileResponse[]>(this.baseUrl);
  }

  getByEngagement(tier: EngagementTier): Observable<AlumniProfileResponse[]> {
    return this.http.get<AlumniProfileResponse[]>(
      `${this.baseUrl}/engagement/${tier}`,
    );
  }

  getByStatus(status: AlumniStatus): Observable<AlumniProfileResponse[]> {
    return this.http.get<AlumniProfileResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  getCommunityOptIn(): Observable<AlumniProfileResponse[]> {
    return this.http.get<AlumniProfileResponse[]>(
      `${this.baseUrl}/community-opt-in`,
    );
  }

  create(
    request: CreateAlumniProfileRequest,
  ): Observable<AlumniProfileResponse> {
    return this.http.post<AlumniProfileResponse>(this.baseUrl, request);
  }

  update(
    alumniId: number,
    request: UpdateAlumniProfileRequest,
  ): Observable<AlumniProfileResponse> {
    return this.http.put<AlumniProfileResponse>(
      `${this.baseUrl}/${alumniId}`,
      request,
    );
  }

  deactivate(alumniId: number): Observable<AlumniProfileResponse> {
    return this.http.patch<AlumniProfileResponse>(
      `${this.baseUrl}/${alumniId}/deactivate`,
      {},
    );
  }

  delete(alumniId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${alumniId}`);
  }
}
