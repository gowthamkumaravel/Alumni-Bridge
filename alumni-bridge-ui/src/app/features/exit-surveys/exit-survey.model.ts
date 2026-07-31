export interface ExitSurveyResponse {
  responseId: number;
  caseId: number;
  employeeId: number;
  employeeName?: string;
  submittedDate?: string;
  managerSatisfactionScore?: number;
  teamCultureScore?: number;
  careerGrowthScore?: number;
  compensationScore?: number;
  overallScore?: number;
  openTextFeedback?: string;
  anonymised?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SubmitExitSurveyRequest {
  caseId: number;
  employeeId: number;
  managerSatisfactionScore?: number;
  teamCultureScore?: number;
  careerGrowthScore?: number;
  compensationScore?: number;
  overallScore?: number;
  openTextFeedback?: string;
  anonymised?: boolean;
}

export interface UpdateExitSurveyRequest {
  managerSatisfactionScore?: number;
  teamCultureScore?: number;
  careerGrowthScore?: number;
  compensationScore?: number;
  overallScore?: number;
  openTextFeedback?: string;
  anonymised?: boolean;
}
