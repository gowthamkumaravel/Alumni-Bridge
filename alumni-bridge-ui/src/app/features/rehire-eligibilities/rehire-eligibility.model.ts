import {
  EligibleForRehire,
  RehireEligibilityStatus,
} from '../../core/models/enums';

export interface RehireEligibilityResponse {
  eligibilityId: number;
  alumniId: number;
  alumniName?: string;
  formerEmployeeId?: string;
  eligibleForRehire: EligibleForRehire;
  coolingOffPeriodMonths?: number;
  eligibilityReviewDate?: string;
  eligibilityReason?: string;
  reviewedById: number;
  reviewedByName?: string;
  status: RehireEligibilityStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRehireEligibilityRequest {
  alumniId: number;
  eligibleForRehire: EligibleForRehire;
  coolingOffPeriodMonths?: number;
  eligibilityReviewDate?: string;
  eligibilityReason?: string;
  reviewedById: number;
  status?: RehireEligibilityStatus;
}

export interface UpdateRehireEligibilityRequest {
  eligibleForRehire?: EligibleForRehire;
  coolingOffPeriodMonths?: number;
  eligibilityReviewDate?: string;
  eligibilityReason?: string;
  reviewedById?: number;
  status?: RehireEligibilityStatus;
}
