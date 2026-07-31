import { ReportMetric, ReportScope } from '../../core/models/enums';

export interface AlumniReportResponse {
  reportId: number;
  scope: ReportScope;
  metric: ReportMetric;
  metricValue?: string;
  generatedDate: string;
}

export interface CreateAlumniReportRequest {
  scope: ReportScope;
  metric: ReportMetric;
  metricValue?: string;
}
