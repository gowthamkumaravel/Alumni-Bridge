import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BoomerangApplicationStatus } from '../../core/models/enums';
import {
  BoomerangApplicationResponse,
  CreateBoomerangApplicationRequest,
  UpdateBoomerangApplicationRequest,
} from './boomerang-application.model';

@Injectable({ providedIn: 'root' })
export class BoomerangApplicationsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/boomerang-applications`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<BoomerangApplicationResponse[]> {
    return this.http.get<BoomerangApplicationResponse[]>(this.baseUrl);
  }

  getByStatus(
    status: BoomerangApplicationStatus,
  ): Observable<BoomerangApplicationResponse[]> {
    return this.http.get<BoomerangApplicationResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateBoomerangApplicationRequest,
  ): Observable<BoomerangApplicationResponse> {
    return this.http.post<BoomerangApplicationResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateBoomerangApplicationRequest,
  ): Observable<BoomerangApplicationResponse> {
    return this.http.put<BoomerangApplicationResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  changeStatus(
    id: number,
    status: BoomerangApplicationStatus,
  ): Observable<BoomerangApplicationResponse> {
    return this.http.patch<BoomerangApplicationResponse>(
      `${this.baseUrl}/${id}/status/${status}`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
