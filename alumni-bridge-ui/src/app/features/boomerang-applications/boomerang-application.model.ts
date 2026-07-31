import { BoomerangApplicationStatus } from '../../core/models/enums';

export interface BoomerangApplicationResponse {
  applicationId: number;
  alumniId: number;
  alumniName?: string;
  formerEmployeeId?: string;
  jobRequisitionId: string;
  applicationDate?: string;
  previousTenureYears?: number;
  recruiterId: number;
  recruiterName?: string;
  status: BoomerangApplicationStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBoomerangApplicationRequest {
  alumniId: number;
  jobRequisitionId: string;
  previousTenureYears?: number;
  recruiterId: number;
}

export interface UpdateBoomerangApplicationRequest {
  jobRequisitionId?: string;
  previousTenureYears?: number;
  recruiterId?: number;
  status?: BoomerangApplicationStatus;
}
