package com.aluminibackend.api.module.alumnireport.service;

import com.aluminibackend.api.module.alumnireport.dto.request.CreateAlumniReportRequest;
import com.aluminibackend.api.module.alumnireport.dto.response.AlumniReportResponse;
import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;

import java.util.List;

public interface AlumniReportService {

    AlumniReportResponse createReport(CreateAlumniReportRequest request);

    AlumniReportResponse getReportById(Long reportId);

    List<AlumniReportResponse> getAllReports();

    List<AlumniReportResponse> getReportsByScope(ReportScope scope);

    List<AlumniReportResponse> getReportsByMetric(ReportMetric metric);

    void deleteReport(Long reportId);
}