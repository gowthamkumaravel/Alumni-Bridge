package com.aluminibackend.api.module.alumnireferral.controller;

import com.aluminibackend.api.module.alumnireferral.dto.request.CreateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.request.UpdateAlumniReferralRequest;
import com.aluminibackend.api.module.alumnireferral.dto.response.AlumniReferralResponse;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import com.aluminibackend.api.module.alumnireferral.service.AlumniReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni-referrals")
@RequiredArgsConstructor
public class AlumniReferralController {

    private final AlumniReferralService referralService;

    @PreAuthorize("@securityAccessService.canAccessAlumniReferrals(#request.alumniId, authentication)")
    @PostMapping
    public ResponseEntity<AlumniReferralResponse> createReferral(
            @Valid @RequestBody CreateAlumniReferralRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(referralService.createReferral(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @GetMapping
    public ResponseEntity<List<AlumniReferralResponse>> getAllReferrals() {
        return ResponseEntity.ok(referralService.getAllReferrals());
    }

    @PreAuthorize("@securityAccessService.canAccessReferral(#referralId, authentication)")
    @GetMapping("/{referralId}")
    public ResponseEntity<AlumniReferralResponse> getReferralById(
            @PathVariable Long referralId
    ) {
        return ResponseEntity.ok(referralService.getReferralById(referralId));
    }

    @PreAuthorize("@securityAccessService.canAccessAlumniReferrals(#alumniId, authentication)")
    @GetMapping("/alumni/{alumniId}")
    public ResponseEntity<List<AlumniReferralResponse>> getReferralsByAlumni(
            @PathVariable Long alumniId
    ) {
        return ResponseEntity.ok(referralService.getReferralsByAlumni(alumniId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @GetMapping("/candidate/{candidateEmail}")
    public ResponseEntity<List<AlumniReferralResponse>> getReferralsByCandidateEmail(
            @PathVariable String candidateEmail
    ) {
        return ResponseEntity.ok(referralService.getReferralsByCandidateEmail(candidateEmail));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @GetMapping("/job/{jobRequisitionId}")
    public ResponseEntity<List<AlumniReferralResponse>> getReferralsByJob(
            @PathVariable String jobRequisitionId
    ) {
        return ResponseEntity.ok(referralService.getReferralsByJob(jobRequisitionId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlumniReferralResponse>> getReferralsByStatus(
            @PathVariable AlumniReferralStatus status
    ) {
        return ResponseEntity.ok(referralService.getReferralsByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @PutMapping("/{referralId}")
    public ResponseEntity<AlumniReferralResponse> updateReferral(
            @PathVariable Long referralId,
            @Valid @RequestBody UpdateAlumniReferralRequest request
    ) {
        return ResponseEntity.ok(referralService.updateReferral(referralId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS')")
    @PatchMapping("/{referralId}/status/{status}")
    public ResponseEntity<AlumniReferralResponse> updateStatus(
            @PathVariable Long referralId,
            @PathVariable AlumniReferralStatus status
    ) {
        return ResponseEntity.ok(referralService.updateStatus(referralId, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{referralId}")
    public ResponseEntity<Void> deleteReferral(
            @PathVariable Long referralId
    ) {
        referralService.deleteReferral(referralId);
        return ResponseEntity.noContent().build();
    }
}
