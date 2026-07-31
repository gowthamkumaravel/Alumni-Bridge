import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ExitSurveyResponse,
  SubmitExitSurveyRequest,
  UpdateExitSurveyRequest,
} from './exit-survey.model';

@Injectable({ providedIn: 'root' })
export class ExitSurveysService {
  private readonly baseUrl = `${environment.apiBaseUrl}/exit-surveys`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<ExitSurveyResponse[]> {
    return this.http.get<ExitSurveyResponse[]>(this.baseUrl);
  }

  getAnonymous(): Observable<ExitSurveyResponse[]> {
    return this.http.get<ExitSurveyResponse[]>(`${this.baseUrl}/anonymous`);
  }

  submit(request: SubmitExitSurveyRequest): Observable<ExitSurveyResponse> {
    return this.http.post<ExitSurveyResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateExitSurveyRequest,
  ): Observable<ExitSurveyResponse> {
    return this.http.put<ExitSurveyResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
