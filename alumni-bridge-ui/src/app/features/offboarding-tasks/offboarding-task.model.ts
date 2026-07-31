import { TaskCategory, TaskStatus } from '../../core/models/enums';

export interface OffboardingTaskResponse {
  taskId: number;
  caseId: number;
  category: TaskCategory;
  taskDescription: string;
  assignedToId: number;
  assignedToName?: string;
  dueDate: string;
  completedDate?: string;
  status: TaskStatus;
}

export interface CreateOffboardingTaskRequest {
  caseId: number;
  category: TaskCategory;
  taskDescription: string;
  assignedToId: number;
  dueDate: string;
}

export interface UpdateOffboardingTaskRequest {
  taskDescription?: string;
  assignedToId?: number;
  dueDate?: string;
  status?: TaskStatus;
}
