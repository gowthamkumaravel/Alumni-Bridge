import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuditLogResponse } from './audit-log.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  // Routed by the gateway to the audit-service.
  private readonly baseUrl = `${environment.apiBaseUrl}/audit-logs`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(this.baseUrl);
  }

  getByEmployee(employeeId: string): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(
      `${this.baseUrl}/employee/${employeeId}`,
    );
  }

  getByModule(module: string): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(
      `${this.baseUrl}/module/${module}`,
    );
  }
}
