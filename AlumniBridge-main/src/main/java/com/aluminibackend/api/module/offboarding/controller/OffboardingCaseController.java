package com.aluminibackend.api.module.offboarding.controller;

import com.aluminibackend.api.module.offboarding.dto.request.CreateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.request.UpdateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.response.OffboardingCaseResponse;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.service.OffboardingCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offboarding-cases")
@RequiredArgsConstructor
public class OffboardingCaseController {

    private final OffboardingCaseService offboardingCaseService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PostMapping
    public ResponseEntity<OffboardingCaseResponse> createCase(
            @Valid @RequestBody CreateOffboardingCaseRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offboardingCaseService.createCase(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<List<OffboardingCaseResponse>> getAllCases() {
        return ResponseEntity.ok(offboardingCaseService.getAllCases());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/{caseId}")
    public ResponseEntity<OffboardingCaseResponse> getCaseById(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(offboardingCaseService.getCaseById(caseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OffboardingCaseResponse>> getCasesByEmployee(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(offboardingCaseService.getCasesByEmployee(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/hrbp/{hrbpOwnerId}")
    public ResponseEntity<List<OffboardingCaseResponse>> getCasesByHrbpOwner(
            @PathVariable Long hrbpOwnerId
    ) {
        return ResponseEntity.ok(offboardingCaseService.getCasesByHrbpOwner(hrbpOwnerId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OffboardingCaseResponse>> getCasesByStatus(
            @PathVariable OffboardingStatus status
    ) {
        return ResponseEntity.ok(offboardingCaseService.getCasesByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PutMapping("/{caseId}")
    public ResponseEntity<OffboardingCaseResponse> updateCase(
            @PathVariable Long caseId,
            @Valid @RequestBody UpdateOffboardingCaseRequest request
    ) {
        return ResponseEntity.ok(offboardingCaseService.updateCase(caseId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PatchMapping("/{caseId}/status/{status}")
    public ResponseEntity<OffboardingCaseResponse> updateStatus(
            @PathVariable Long caseId,
            @PathVariable OffboardingStatus status
    ) {
        return ResponseEntity.ok(offboardingCaseService.updateStatus(caseId, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> deleteCase(
            @PathVariable Long caseId
    ) {
        offboardingCaseService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }
}