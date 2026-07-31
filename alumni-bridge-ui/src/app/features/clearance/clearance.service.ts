import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClearanceStatus } from '../../core/models/enums';
import {
  ClearanceCertificateResponse,
  CreateClearanceCertificateRequest,
} from './clearance-certificate.model';

@Injectable({ providedIn: 'root' })
export class ClearanceService {
  private readonly baseUrl = `${environment.apiBaseUrl}/clearance-certificates`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<ClearanceCertificateResponse[]> {
    return this.http.get<ClearanceCertificateResponse[]>(this.baseUrl);
  }

  getByStatus(
    status: ClearanceStatus,
  ): Observable<ClearanceCertificateResponse[]> {
    return this.http.get<ClearanceCertificateResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateClearanceCertificateRequest,
  ): Observable<ClearanceCertificateResponse> {
    return this.http.post<ClearanceCertificateResponse>(this.baseUrl, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
