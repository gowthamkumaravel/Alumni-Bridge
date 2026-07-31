package com.aluminibackend.api.module.alumnireport.mapper;

import com.aluminibackend.api.module.alumnireport.dto.response.AlumniReportResponse;
import com.aluminibackend.api.module.alumnireport.entity.AlumniReport;
import org.springframework.stereotype.Component;

@Component
public class AlumniReportMapper {

    public AlumniReportResponse toResponse(AlumniReport report) {
        return AlumniReportResponse.builder()
                .reportId(report.getReportId())
                .scope(report.getScope())
                .metric(report.getMetric())
                .metricValue(report.getMetricValue())
                .generatedDate(report.getGeneratedDate())
                .build();
    }
}