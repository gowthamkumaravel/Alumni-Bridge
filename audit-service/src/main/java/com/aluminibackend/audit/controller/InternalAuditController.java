package com.aluminibackend.audit.controller;

import com.aluminibackend.audit.dto.RecordAuditRequest;
import com.aluminibackend.audit.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Service-to-service write API. Reachable only with a valid X-Internal-Token
 * (enforced by InternalAuthFilter). Used by the monolith's Feign adapter.
 */
@RestController
@RequestMapping("/internal/audit-logs")
@RequiredArgsConstructor
public class InternalAuditController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<Void> record(@Valid @RequestBody RecordAuditRequest request) {
        auditLogService.record(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
