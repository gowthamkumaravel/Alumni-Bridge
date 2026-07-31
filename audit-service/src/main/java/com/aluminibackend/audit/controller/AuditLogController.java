package com.aluminibackend.audit.controller;

import com.aluminibackend.audit.dto.AuditLogResponse;
import com.aluminibackend.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public read API. Same paths and ADMIN-only rules as the original monolith
 * controller, so callers/consumers are unaffected.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByUser(@PathVariable String employeeId) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(employeeId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/module/{module}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByModule(@PathVariable String module) {
        return ResponseEntity.ok(auditLogService.getLogsByModule(module));
    }
}
