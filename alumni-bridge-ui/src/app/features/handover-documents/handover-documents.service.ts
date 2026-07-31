import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DocumentStatus, DocumentType } from '../../core/models/enums';
import {
  CreateHandoverDocumentRequest,
  HandoverDocumentResponse,
  UpdateHandoverDocumentRequest,
} from './handover-document.model';

@Injectable({ providedIn: 'root' })
export class HandoverDocumentsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/handover-documents`;

  constructor(private readonly http: HttpClient) {}

  // No "list all" endpoint; documents are fetched by handover, type or status.
  getByHandover(handoverId: number): Observable<HandoverDocumentResponse[]> {
    return this.http.get<HandoverDocumentResponse[]>(
      `${this.baseUrl}/handover/${handoverId}`,
    );
  }

  getByType(type: DocumentType): Observable<HandoverDocumentResponse[]> {
    return this.http.get<HandoverDocumentResponse[]>(
      `${this.baseUrl}/type/${type}`,
    );
  }

  getByStatus(status: DocumentStatus): Observable<HandoverDocumentResponse[]> {
    return this.http.get<HandoverDocumentResponse[]>(
      `${this.baseUrl}/status/${status}`,
    );
  }

  create(
    request: CreateHandoverDocumentRequest,
  ): Observable<HandoverDocumentResponse> {
    return this.http.post<HandoverDocumentResponse>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdateHandoverDocumentRequest,
  ): Observable<HandoverDocumentResponse> {
    return this.http.put<HandoverDocumentResponse>(
      `${this.baseUrl}/${id}`,
      request,
    );
  }

  markReviewed(id: number): Observable<HandoverDocumentResponse> {
    return this.http.patch<HandoverDocumentResponse>(
      `${this.baseUrl}/${id}/review`,
      {},
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
