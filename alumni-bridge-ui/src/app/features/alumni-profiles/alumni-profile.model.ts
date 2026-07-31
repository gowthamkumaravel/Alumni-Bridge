import { AlumniStatus, EngagementTier } from '../../core/models/enums';

export interface AlumniProfileResponse {
  alumniId: number;
  employeeId: string;
  formerEmployeeId?: string;
  name?: string;
  email?: string;
  lastRole?: string;
  departureDateFrom?: string;
  currentOrganisation?: string;
  currentRole?: string;
  linkedInRef?: string;
  engagementTier?: EngagementTier;
  communityOptIn?: boolean;
  status: AlumniStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAlumniProfileRequest {
  employeeId: string;
  lastRole?: string;
  departureDateFrom?: string;
  currentOrganisation?: string;
  currentRole?: string;
  linkedInRef?: string;
  engagementTier?: EngagementTier;
  communityOptIn?: boolean;
}

export interface UpdateAlumniProfileRequest {
  lastRole?: string;
  currentOrganisation?: string;
  currentRole?: string;
  linkedInRef?: string;
  engagementTier?: EngagementTier;
  communityOptIn?: boolean;
  status?: AlumniStatus;
}
