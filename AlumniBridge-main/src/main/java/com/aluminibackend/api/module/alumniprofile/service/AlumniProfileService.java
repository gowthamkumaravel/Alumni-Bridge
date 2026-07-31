package com.aluminibackend.api.module.alumniprofile.service;

import com.aluminibackend.api.module.alumniprofile.dto.request.CreateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.request.UpdateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.response.AlumniProfileResponse;
import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;

import java.util.List;

public interface AlumniProfileService {

    AlumniProfileResponse createProfile(CreateAlumniProfileRequest request);

    AlumniProfileResponse getProfileById(Long alumniId);

    AlumniProfileResponse getProfileByUser(String employeeId);

    AlumniProfileResponse getProfileByFormerEmployeeId(String formerEmployeeId);

    List<AlumniProfileResponse> getAllProfiles();

    List<AlumniProfileResponse> getProfilesByEngagementTier(EngagementTier engagementTier);

    List<AlumniProfileResponse> getProfilesByStatus(AlumniStatus status);

    List<AlumniProfileResponse> getCommunityOptInProfiles();

    AlumniProfileResponse updateProfile(Long alumniId, UpdateAlumniProfileRequest request);

    AlumniProfileResponse deactivateProfile(Long alumniId);

    void deleteProfile(Long alumniId);
}