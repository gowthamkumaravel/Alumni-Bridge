import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UserRole, UserStatus } from '../../core/models/enums';
import {
  CreateUserRequest,
  UpdateUserRequest,
  UserResponse,
} from './user.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.baseUrl);
  }

  getByEmployeeId(employeeId: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/${employeeId}`);
  }

  getByRole(role: UserRole): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}/role/${role}`);
  }

  getByStatus(status: UserStatus): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}/status/${status}`);
  }

  create(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.baseUrl, request);
  }

  update(
    employeeId: string,
    request: UpdateUserRequest,
  ): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/${employeeId}`, request);
  }

  deactivate(employeeId: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.baseUrl}/${employeeId}/deactivate`,
      {},
    );
  }

  convertToAlumni(employeeId: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.baseUrl}/${employeeId}/convert-to-alumni`,
      {},
    );
  }

  delete(employeeId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${employeeId}`);
  }
}
