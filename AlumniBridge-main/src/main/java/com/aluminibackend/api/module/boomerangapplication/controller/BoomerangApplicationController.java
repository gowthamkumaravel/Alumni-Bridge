package com.aluminibackend.api.module.boomerangapplication.controller;

import com.aluminibackend.api.module.boomerangapplication.dto.request.CreateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.request.UpdateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.response.BoomerangApplicationResponse;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import com.aluminibackend.api.module.boomerangapplication.service.BoomerangApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boomerang-applications")
@RequiredArgsConstructor
public class BoomerangApplicationController {

    private final BoomerangApplicationService applicationService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PostMapping
    public ResponseEntity<BoomerangApplicationResponse> createApplication(
            @Valid @RequestBody CreateBoomerangApplicationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(applicationService.createApplication(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<BoomerangApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<BoomerangApplicationResponse> getApplicationById(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationById(applicationId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/alumni/{alumniId}")
    public ResponseEntity<List<BoomerangApplicationResponse>> getApplicationsByAlumni(
            @PathVariable Long alumniId
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByAlumni(alumniId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<BoomerangApplicationResponse>> getApplicationsByRecruiter(
            @PathVariable Long recruiterId
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByRecruiter(recruiterId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/job/{jobRequisitionId}")
    public ResponseEntity<List<BoomerangApplicationResponse>> getApplicationsByJob(
            @PathVariable String jobRequisitionId
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByJob(jobRequisitionId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BoomerangApplicationResponse>> getApplicationsByStatus(
            @PathVariable BoomerangApplicationStatus status
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationsByStatus(status)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PutMapping("/{applicationId}")
    public ResponseEntity<BoomerangApplicationResponse> updateApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateBoomerangApplicationRequest request
    ) {
        return ResponseEntity.ok(
                applicationService.updateApplication(applicationId, request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PatchMapping("/{applicationId}/status/{status}")
    public ResponseEntity<BoomerangApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @PathVariable BoomerangApplicationStatus status
    ) {
        return ResponseEntity.ok(
                applicationService.updateStatus(applicationId, status)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long applicationId
    ) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.noContent().build();
    }
}