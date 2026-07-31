package com.aluminibackend.api.module.clearance.controller;

import com.aluminibackend.api.module.clearance.dto.request.CreateClearanceCertificateRequest;
import com.aluminibackend.api.module.clearance.dto.response.ClearanceCertificateResponse;
import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;
import com.aluminibackend.api.module.clearance.service.ClearanceCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clearance-certificates")
@RequiredArgsConstructor
public class ClearanceCertificateController {

    private final ClearanceCertificateService clearanceService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PostMapping
    public ResponseEntity<ClearanceCertificateResponse> issueCertificate(
            @Valid @RequestBody CreateClearanceCertificateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clearanceService.issueCertificate(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<List<ClearanceCertificateResponse>> getAllCertificates() {
        return ResponseEntity.ok(clearanceService.getAllCertificates());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/{certId}")
    public ResponseEntity<ClearanceCertificateResponse> getCertificateById(
            @PathVariable Long certId
    ) {
        return ResponseEntity.ok(clearanceService.getCertificateById(certId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ClearanceCertificateResponse> getCertificateByCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(clearanceService.getCertificateByCase(caseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClearanceCertificateResponse>> getCertificatesByStatus(
            @PathVariable ClearanceStatus status
    ) {
        return ResponseEntity.ok(clearanceService.getCertificatesByStatus(status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{certId}")
    public ResponseEntity<Void> deleteCertificate(
            @PathVariable Long certId
    ) {
        clearanceService.deleteCertificate(certId);
        return ResponseEntity.noContent().build();
    }
}