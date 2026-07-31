package com.aluminibackend.api.module.alumnireport.repository;

import com.aluminibackend.api.module.alumnireport.entity.AlumniReport;
import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumniReportRepository extends JpaRepository<AlumniReport, Long> {

    List<AlumniReport> findByScope(ReportScope scope);

    List<AlumniReport> findByMetric(ReportMetric metric);
}