import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateOffboardingTaskRequest,
  OffboardingTaskResponse,
  UpdateOffboardingTaskRequest,
} from './offboarding-task.model';

@Injectable({ providedIn: 'root' })
export class OffboardingTasksService {
  private readonly baseUrl = `${environment.apiBaseUrl}/offboarding-tasks`;

  constructor(private readonly http: HttpClient) {}

  // There is no "list all" endpoint on the backend; tasks are scoped to a case or assignee.
  getByCase(caseId: number): Observable<OffboardingTaskResponse[]> {
    return this.http.get<OffboardingTaskResponse[]>(
      `${this.baseUrl}/case/${caseId}`,
    );
  }

  getByAssignee(userId: number): Observable<OffboardingTaskResponse[]> {
    return this.http.get<OffboardingTaskResponse[]>(
      `${this.baseUrl}/assignee/${userId}`,
    );
  }

  create(
    request: CreateOffboardingTaskRequest,
  ): Observable<OffboardingTaskResponse> {
    return this.http.post<OffboardingTaskResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateOffboardingTaskRequest,
  ): Observable<OffboardingTaskResponse> {
    return this.http.put<OffboardingTaskResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  complete(id: number): Observable<OffboardingTaskResponse> {
    return this.http.patch<OffboardingTaskResponse>(
      `${this.baseUrl}/${id}/complete`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
