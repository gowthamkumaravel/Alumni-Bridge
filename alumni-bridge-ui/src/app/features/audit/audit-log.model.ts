export interface AuditLogResponse {
  auditId: number;
  employeeId: string;
  userName?: string;
  action: string;
  module: string;
  timestamp: string;
}
