import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ReportMetric, ReportScope } from '../../core/models/enums';
import {
  AlumniReportResponse,
  CreateAlumniReportRequest,
} from './alumni-report.model';

@Injectable({ providedIn: 'root' })
export class AlumniReportsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/alumni-reports`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AlumniReportResponse[]> {
    return this.http.get<AlumniReportResponse[]>(this.baseUrl);
  }

  getByScope(scope: ReportScope): Observable<AlumniReportResponse[]> {
    return this.http.get<AlumniReportResponse[]>(
      `${this.baseUrl}/scope/${scope}`,
    );
  }

  getByMetric(metric: ReportMetric): Observable<AlumniReportResponse[]> {
    return this.http.get<AlumniReportResponse[]>(
      `${this.baseUrl}/metric/${metric}`,
    );
  }

  create(request: CreateAlumniReportRequest): Observable<AlumniReportResponse> {
    return this.http.post<AlumniReportResponse>(this.baseUrl, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
