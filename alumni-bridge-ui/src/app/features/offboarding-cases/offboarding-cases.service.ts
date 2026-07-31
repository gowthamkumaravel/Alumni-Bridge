import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { OffboardingStatus } from '../../core/models/enums';
import {
  CreateOffboardingCaseRequest,
  OffboardingCaseResponse,
  UpdateOffboardingCaseRequest,
} from './offboarding-case.model';

@Injectable({ providedIn: 'root' })
export class OffboardingCasesService {
  private readonly baseUrl = `${environment.apiBaseUrl}/offboarding-cases`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<OffboardingCaseResponse[]> {
    return this.http.get<OffboardingCaseResponse[]>(this.baseUrl);
  }

  getById(caseId: number): Observable<OffboardingCaseResponse> {
    return this.http.get<OffboardingCaseResponse>(`${this.baseUrl}/${caseId}`);
  }

  getByStatus(
    status: OffboardingStatus,
  ): Observable<OffboardingCaseResponse[]> {
    return this.http.get<OffboardingCaseResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateOffboardingCaseRequest,
  ): Observable<OffboardingCaseResponse> {
    return this.http.post<OffboardingCaseResponse>(this.baseUrl, request);
  }

  update(
    caseId: number,
    request: UpdateOffboardingCaseRequest,
  ): Observable<OffboardingCaseResponse> {
    return this.http.put<OffboardingCaseResponse>(
      `${this.baseUrl}/${caseId}`,
      request,
    );
  }

  changeStatus(
    caseId: number,
    status: OffboardingStatus,
  ): Observable<OffboardingCaseResponse> {
    return this.http.patch<OffboardingCaseResponse>(
      `${this.baseUrl}/${caseId}/status/${status}`,
      {},
    );
  }

  delete(caseId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${caseId}`);
  }
}
