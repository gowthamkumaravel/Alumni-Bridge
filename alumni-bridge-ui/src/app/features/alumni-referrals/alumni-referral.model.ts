import { AlumniReferralStatus } from '../../core/models/enums';

export interface AlumniReferralResponse {
  referralId: number;
  alumniId: number;
  alumniName?: string;
  formerEmployeeId?: string;
  candidateName: string;
  candidateEmail: string;
  jobRequisitionId: string;
  referralDate?: string;
  status: AlumniReferralStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAlumniReferralRequest {
  alumniId: number;
  candidateName: string;
  candidateEmail: string;
  jobRequisitionId: string;
}

export interface UpdateAlumniReferralRequest {
  candidateName?: string;
  candidateEmail?: string;
  jobRequisitionId?: string;
  status?: AlumniReferralStatus;
}
