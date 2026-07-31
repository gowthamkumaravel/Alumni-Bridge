package com.aluminibackend.api.module.alumnireport.controller;

import com.aluminibackend.api.module.alumnireport.dto.request.CreateAlumniReportRequest;
import com.aluminibackend.api.module.alumnireport.dto.response.AlumniReportResponse;
import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import com.aluminibackend.api.module.alumnireport.service.AlumniReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni-reports")
@RequiredArgsConstructor
public class AlumniReportController {

    private final AlumniReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @PostMapping
    public ResponseEntity<AlumniReportResponse> createReport(
            @Valid @RequestBody CreateAlumniReportRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reportService.createReport(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<AlumniReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping("/{reportId}")
    public ResponseEntity<AlumniReportResponse> getReportById(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(reportService.getReportById(reportId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping("/scope/{scope}")
    public ResponseEntity<List<AlumniReportResponse>> getReportsByScope(
            @PathVariable ReportScope scope
    ) {
        return ResponseEntity.ok(reportService.getReportsByScope(scope));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping("/metric/{metric}")
    public ResponseEntity<List<AlumniReportResponse>> getReportsByMetric(
            @PathVariable ReportMetric metric
    ) {
        return ResponseEntity.ok(reportService.getReportsByMetric(metric));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long reportId
    ) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}