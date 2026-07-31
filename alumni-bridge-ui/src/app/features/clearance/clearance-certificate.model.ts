import { ClearanceStatus } from '../../core/models/enums';

export interface ClearanceCertificateResponse {
  certId: number;
  caseId: number;
  employeeId: number;
  employeeName?: string;
  issuedDate?: string;
  issuedById: number;
  issuedByName?: string;
  outstandingItems?: string;
  status: ClearanceStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateClearanceCertificateRequest {
  caseId: number;
  issuedById: number;
}
