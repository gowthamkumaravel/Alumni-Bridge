package com.aluminibackend.api.module.alumnireport.dto.request;

import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAlumniReportRequest {

    @NotNull(message = "Scope is required")
    private ReportScope scope;

    @NotNull(message = "Metric is required")
    private ReportMetric metric;

    private String metricValue;
}