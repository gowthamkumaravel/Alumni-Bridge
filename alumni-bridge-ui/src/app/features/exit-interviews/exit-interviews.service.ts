import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ExitInterviewStatus } from '../../core/models/enums';
import {
  CompleteExitInterviewRequest,
  CreateExitInterviewRequest,
  ExitInterviewResponse,
  UpdateExitInterviewRequest,
} from './exit-interview.model';

@Injectable({ providedIn: 'root' })
export class ExitInterviewsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/exit-interviews`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<ExitInterviewResponse[]> {
    return this.http.get<ExitInterviewResponse[]>(this.baseUrl);
  }

  getByStatus(
    status: ExitInterviewStatus,
  ): Observable<ExitInterviewResponse[]> {
    return this.http.get<ExitInterviewResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateExitInterviewRequest,
  ): Observable<ExitInterviewResponse> {
    return this.http.post<ExitInterviewResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateExitInterviewRequest,
  ): Observable<ExitInterviewResponse> {
    return this.http.put<ExitInterviewResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  complete(
    id: number,
    request: CompleteExitInterviewRequest,
  ): Observable<ExitInterviewResponse> {
    return this.http.patch<ExitInterviewResponse>(
      `${this.baseUrl}/${id}/complete`,
      request,
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
