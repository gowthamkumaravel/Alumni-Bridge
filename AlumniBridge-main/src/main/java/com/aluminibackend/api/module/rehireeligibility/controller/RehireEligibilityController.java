package com.aluminibackend.api.module.rehireeligibility.controller;

import com.aluminibackend.api.module.rehireeligibility.dto.request.CreateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.request.UpdateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.response.RehireEligibilityResponse;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import com.aluminibackend.api.module.rehireeligibility.service.RehireEligibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rehire-eligibilities")
@RequiredArgsConstructor
public class RehireEligibilityController {

    private final RehireEligibilityService eligibilityService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PostMapping
    public ResponseEntity<RehireEligibilityResponse> createEligibility(
            @Valid @RequestBody CreateRehireEligibilityRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eligibilityService.createEligibility(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<RehireEligibilityResponse>> getAllEligibilities() {
        return ResponseEntity.ok(eligibilityService.getAllEligibilities());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/{eligibilityId}")
    public ResponseEntity<RehireEligibilityResponse> getEligibilityById(
            @PathVariable Long eligibilityId
    ) {
        return ResponseEntity.ok(
                eligibilityService.getEligibilityById(eligibilityId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/alumni/{alumniId}")
    public ResponseEntity<RehireEligibilityResponse> getEligibilityByAlumni(
            @PathVariable Long alumniId
    ) {
        return ResponseEntity.ok(
                eligibilityService.getEligibilityByAlumni(alumniId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/eligible-for-rehire/{eligibleForRehire}")
    public ResponseEntity<List<RehireEligibilityResponse>> getByEligibleForRehire(
            @PathVariable EligibleForRehire eligibleForRehire
    ) {
        return ResponseEntity.ok(
                eligibilityService.getByEligibleForRehire(eligibleForRehire)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RehireEligibilityResponse>> getByStatus(
            @PathVariable RehireEligibilityStatus status
    ) {
        return ResponseEntity.ok(
                eligibilityService.getByStatus(status)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @GetMapping("/reviewer/{reviewedById}")
    public ResponseEntity<List<RehireEligibilityResponse>> getByReviewer(
            @PathVariable Long reviewedById
    ) {
        return ResponseEntity.ok(
                eligibilityService.getByReviewer(reviewedById)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PutMapping("/{eligibilityId}")
    public ResponseEntity<RehireEligibilityResponse> updateEligibility(
            @PathVariable Long eligibilityId,
            @Valid @RequestBody UpdateRehireEligibilityRequest request
    ) {
        return ResponseEntity.ok(
                eligibilityService.updateEligibility(eligibilityId, request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PatchMapping("/{eligibilityId}/mark-eligible")
    public ResponseEntity<RehireEligibilityResponse> markEligible(
            @PathVariable Long eligibilityId
    ) {
        return ResponseEntity.ok(
                eligibilityService.markEligible(eligibilityId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PatchMapping("/{eligibilityId}/mark-ineligible")
    public ResponseEntity<RehireEligibilityResponse> markIneligible(
            @PathVariable Long eligibilityId
    ) {
        return ResponseEntity.ok(
                eligibilityService.markIneligible(eligibilityId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'TALENT_ACQUISITION')")
    @PatchMapping("/{eligibilityId}/under-review")
    public ResponseEntity<RehireEligibilityResponse> markUnderReview(
            @PathVariable Long eligibilityId
    ) {
        return ResponseEntity.ok(
                eligibilityService.markUnderReview(eligibilityId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{eligibilityId}")
    public ResponseEntity<Void> deleteEligibility(
            @PathVariable Long eligibilityId
    ) {
        eligibilityService.deleteEligibility(eligibilityId);
        return ResponseEntity.noContent().build();
    }
}