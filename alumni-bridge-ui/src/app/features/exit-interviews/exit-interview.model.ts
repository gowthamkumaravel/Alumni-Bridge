import { ExitInterviewStatus, InterviewFormat } from '../../core/models/enums';

export interface ExitInterviewResponse {
  interviewId: number;
  caseId: number;
  employeeId: string;
  employeeName?: string;
  interviewerId: string;
  interviewerName?: string;
  scheduledDate: string;
  format: InterviewFormat;
  primaryReasonForLeaving?: string;
  keyFeedback?: string;
  status: ExitInterviewStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateExitInterviewRequest {
  caseId: number;
  interviewerId: string;
  scheduledDate: string;
  format: InterviewFormat;
}

export interface UpdateExitInterviewRequest {
  interviewerId?: string;
  scheduledDate?: string;
  format?: InterviewFormat;
  primaryReasonForLeaving?: string;
  keyFeedback?: string;
  status?: ExitInterviewStatus;
}

export interface CompleteExitInterviewRequest {
  primaryReasonForLeaving: string;
  keyFeedback: string;
}
