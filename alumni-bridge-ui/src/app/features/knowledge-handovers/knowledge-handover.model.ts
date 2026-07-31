import { HandoverStatus } from '../../core/models/enums';

export interface KnowledgeHandoverResponse {
  handoverId: number;
  caseId: number;
  employeeId: number;
  employeeName?: string;
  successorId: number;
  successorName?: string;
  activeProjectsSummary?: string;
  keyContactsRef?: string;
  systemsAccessDetails?: string;
  criticalProcessNotes?: string;
  completionDeadline?: string;
  status: HandoverStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateKnowledgeHandoverRequest {
  caseId: number;
  successorId: number;
  activeProjectsSummary?: string;
  keyContactsRef?: string;
  systemsAccessDetails?: string;
  criticalProcessNotes?: string;
  completionDeadline?: string;
}

export interface UpdateKnowledgeHandoverRequest {
  successorId?: number;
  activeProjectsSummary?: string;
  keyContactsRef?: string;
  systemsAccessDetails?: string;
  criticalProcessNotes?: string;
  completionDeadline?: string;
  status?: HandoverStatus;
}
