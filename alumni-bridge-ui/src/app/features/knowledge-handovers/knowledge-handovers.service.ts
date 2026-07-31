import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { HandoverStatus } from '../../core/models/enums';
import {
  CreateKnowledgeHandoverRequest,
  KnowledgeHandoverResponse,
  UpdateKnowledgeHandoverRequest,
} from './knowledge-handover.model';

@Injectable({ providedIn: 'root' })
export class KnowledgeHandoversService {
  private readonly baseUrl = `${environment.apiBaseUrl}/knowledge-handovers`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<KnowledgeHandoverResponse[]> {
    return this.http.get<KnowledgeHandoverResponse[]>(this.baseUrl);
  }

  getByStatus(status: HandoverStatus): Observable<KnowledgeHandoverResponse[]> {
    return this.http.get<KnowledgeHandoverResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateKnowledgeHandoverRequest,
  ): Observable<KnowledgeHandoverResponse> {
    return this.http.post<KnowledgeHandoverResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateKnowledgeHandoverRequest,
  ): Observable<KnowledgeHandoverResponse> {
    return this.http.put<KnowledgeHandoverResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  complete(id: number): Observable<KnowledgeHandoverResponse> {
    return this.http.patch<KnowledgeHandoverResponse>(
      `${this.baseUrl}/${id}/complete`,
      {},
    );
  }

  managerSignoff(id: number): Observable<KnowledgeHandoverResponse> {
    return this.http.patch<KnowledgeHandoverResponse>(
      `${this.baseUrl}/${id}/manager-signoff`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
