package com.aluminibackend.api.module.alumnireport.dto.response;

import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlumniReportResponse {

    private Long reportId;

    private ReportScope scope;

    private ReportMetric metric;

    private String metricValue;

    private LocalDateTime generatedDate;
}