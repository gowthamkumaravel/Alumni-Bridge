import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RehireEligibilityStatus } from '../../core/models/enums';
import {
  CreateRehireEligibilityRequest,
  RehireEligibilityResponse,
  UpdateRehireEligibilityRequest,
} from './rehire-eligibility.model';

@Injectable({ providedIn: 'root' })
export class RehireEligibilitiesService {
  private readonly baseUrl = `${environment.apiBaseUrl}/rehire-eligibilities`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<RehireEligibilityResponse[]> {
    return this.http.get<RehireEligibilityResponse[]>(this.baseUrl);
  }

  getByStatus(
    status: RehireEligibilityStatus,
  ): Observable<RehireEligibilityResponse[]> {
    return this.http.get<RehireEligibilityResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateRehireEligibilityRequest,
  ): Observable<RehireEligibilityResponse> {
    return this.http.post<RehireEligibilityResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateRehireEligibilityRequest,
  ): Observable<RehireEligibilityResponse> {
    return this.http.put<RehireEligibilityResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  markEligible(id: number): Observable<RehireEligibilityResponse> {
    return this.http.patch<RehireEligibilityResponse>(
      `${this.baseUrl}/${id}/mark-eligible`,
      {},
    );
  }

  markIneligible(id: number): Observable<RehireEligibilityResponse> {
    return this.http.patch<RehireEligibilityResponse>(
      `${this.baseUrl}/${id}/mark-ineligible`,
      {},
    );
  }

  underReview(id: number): Observable<RehireEligibilityResponse> {
    return this.http.patch<RehireEligibilityResponse>(
      `${this.baseUrl}/${id}/under-review`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
