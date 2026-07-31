import { DocumentStatus, DocumentType } from '../../core/models/enums';

export interface HandoverDocumentResponse {
  docId: number;
  handoverId: number;
  caseId?: number;
  documentType: DocumentType;
  filePath: string;
  uploadedDate?: string;
  status: DocumentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateHandoverDocumentRequest {
  handoverId: number;
  documentType: DocumentType;
  filePath: string;
}

export interface UpdateHandoverDocumentRequest {
  documentType?: DocumentType;
  filePath?: string;
  status?: DocumentStatus;
}
