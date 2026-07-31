import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AlumniReferralStatus } from '../../core/models/enums';
import {
  AlumniReferralResponse,
  CreateAlumniReferralRequest,
  UpdateAlumniReferralRequest,
} from './alumni-referral.model';

@Injectable({ providedIn: 'root' })
export class AlumniReferralsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/alumni-referrals`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AlumniReferralResponse[]> {
    return this.http.get<AlumniReferralResponse[]>(this.baseUrl);
  }

  getByStatus(
    status: AlumniReferralStatus,
  ): Observable<AlumniReferralResponse[]> {
    return this.http.get<AlumniReferralResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateAlumniReferralRequest,
  ): Observable<AlumniReferralResponse> {
    return this.http.post<AlumniReferralResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateAlumniReferralRequest,
  ): Observable<AlumniReferralResponse> {
    return this.http.put<AlumniReferralResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  changeStatus(
    id: number,
    status: AlumniReferralStatus,
  ): Observable<AlumniReferralResponse> {
    return this.http.patch<AlumniReferralResponse>(
      `${this.baseUrl}/${id}/status/${status}`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
