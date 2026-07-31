import { DepartureType, OffboardingStatus } from '../../core/models/enums';

export interface OffboardingCaseResponse {
  caseId: number;
  employeeId: number;
  employeeName?: string;
  employeeEmail?: string;
  departureType: DepartureType;
  lastWorkingDate: string;
  hrbpOwnerId: number;
  hrbpOwnerName?: string;
  status: OffboardingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOffboardingCaseRequest {
  employeeId: number;
  departureType: DepartureType;
  lastWorkingDate: string;
  hrbpOwnerId: number;
}

export interface UpdateOffboardingCaseRequest {
  departureType?: DepartureType;
  lastWorkingDate?: string;
  hrbpOwnerId?: number;
  status?: OffboardingStatus;
}
