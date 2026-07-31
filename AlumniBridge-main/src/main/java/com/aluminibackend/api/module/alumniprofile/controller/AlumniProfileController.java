package com.aluminibackend.api.module.alumniprofile.controller;

import com.aluminibackend.api.module.alumniprofile.dto.request.CreateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.request.UpdateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.response.AlumniProfileResponse;
import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import com.aluminibackend.api.module.alumniprofile.service.AlumniProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni-profiles")
@RequiredArgsConstructor
public class AlumniProfileController {

    private final AlumniProfileService profileService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PostMapping
    public ResponseEntity<AlumniProfileResponse> createProfile(
            @Valid @RequestBody CreateAlumniProfileRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profileService.createProfile(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping
    public ResponseEntity<List<AlumniProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @PreAuthorize("@securityAccessService.canAccessAlumniProfile(#alumniId, authentication)")
    @GetMapping("/{alumniId}")
    public ResponseEntity<AlumniProfileResponse> getProfileById(
            @PathVariable Long alumniId
    ) {
        return ResponseEntity.ok(profileService.getProfileById(alumniId));
    }

    @PreAuthorize("@securityAccessService.canAccessUser(#employeeId, authentication)")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<AlumniProfileResponse> getProfileByUser(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(profileService.getProfileByUser(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION')")
    @GetMapping("/former-employee/{formerEmployeeId}")
    public ResponseEntity<AlumniProfileResponse> getProfileByFormerEmployeeId(
            @PathVariable String formerEmployeeId
    ) {
        return ResponseEntity.ok(profileService.getProfileByFormerEmployeeId(formerEmployeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS')")
    @GetMapping("/engagement/{engagementTier}")
    public ResponseEntity<List<AlumniProfileResponse>> getProfilesByEngagementTier(
            @PathVariable EngagementTier engagementTier
    ) {
        return ResponseEntity.ok(profileService.getProfilesByEngagementTier(engagementTier));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlumniProfileResponse>> getProfilesByStatus(
            @PathVariable AlumniStatus status
    ) {
        return ResponseEntity.ok(profileService.getProfilesByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS')")
    @GetMapping("/community-opt-in")
    public ResponseEntity<List<AlumniProfileResponse>> getCommunityOptInProfiles() {
        return ResponseEntity.ok(profileService.getCommunityOptInProfiles());
    }

    @PreAuthorize("@securityAccessService.canAccessAlumniProfile(#alumniId, authentication)")
    @PutMapping("/{alumniId}")
    public ResponseEntity<AlumniProfileResponse> updateProfile(
            @PathVariable Long alumniId,
            @Valid @RequestBody UpdateAlumniProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(alumniId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'ALUMNI_RELATIONS')")
    @PatchMapping("/{alumniId}/deactivate")
    public ResponseEntity<AlumniProfileResponse> deactivateProfile(
            @PathVariable Long alumniId
    ) {
        return ResponseEntity.ok(profileService.deactivateProfile(alumniId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{alumniId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Long alumniId
    ) {
        profileService.deleteProfile(alumniId);
        return ResponseEntity.noContent().build();
    }
}
