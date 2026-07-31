package com.aluminibackend.api.module.alumniprofile.service;

import com.aluminibackend.api.module.alumniprofile.dto.request.CreateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.request.UpdateAlumniProfileRequest;
import com.aluminibackend.api.module.alumniprofile.dto.response.AlumniProfileResponse;
import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import com.aluminibackend.api.module.alumniprofile.mapper.AlumniProfileMapper;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniProfileServiceImpl implements AlumniProfileService {

    private final AlumniProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AlumniProfileMapper mapper;

    @Override
    public AlumniProfileResponse createProfile(CreateAlumniProfileRequest request) {

        if (profileRepository.existsByUser_EmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Alumni profile already exists for this user");
        }

        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AlumniProfile profile = AlumniProfile.builder()
                .user(user)
                .formerEmployeeId(user.getEmployeeId())
                .name(user.getName())
                .lastRole(request.getLastRole())
                .departureDateFrom(request.getDepartureDateFrom())
                .currentOrganisation(request.getCurrentOrganisation())
                .currentRole(request.getCurrentRole())
                .linkedInRef(request.getLinkedInRef())
                .engagementTier(
                        request.getEngagementTier() != null
                                ? request.getEngagementTier()
                                : EngagementTier.PASSIVE
                )
                .communityOptIn(
                        request.getCommunityOptIn() != null
                                ? request.getCommunityOptIn()
                                : true
                )
                .status(AlumniStatus.ACTIVE)
                .build();

        user.setAccountType(AccountType.ALUMNI);
        user.setStatus(UserStatus.ALUMNI);

        userRepository.save(user);

        AlumniProfile savedProfile = profileRepository.save(profile);

        return mapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileResponse getProfileById(Long alumniId) {
        return mapper.toResponse(findProfileById(alumniId));
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileResponse getProfileByUser(String employeeId) {
        AlumniProfile profile = profileRepository.findByUser_EmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni profile not found for employee ID: " + employeeId
                ));

        return mapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileResponse getProfileByFormerEmployeeId(String formerEmployeeId) {
        AlumniProfile profile = profileRepository.findByFormerEmployeeId(formerEmployeeId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni profile not found for employee ID: " + formerEmployeeId
                ));

        return mapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getAllProfiles() {
        return profileRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getProfilesByEngagementTier(
            EngagementTier engagementTier
    ) {
        return profileRepository.findByEngagementTier(engagementTier)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getProfilesByStatus(
            AlumniStatus status
    ) {
        return profileRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getCommunityOptInProfiles() {
        return profileRepository.findByCommunityOptInIs(true)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AlumniProfileResponse updateProfile(
            Long alumniId,
            UpdateAlumniProfileRequest request
    ) {
        AlumniProfile profile = findProfileById(alumniId);

        if (request.getLastRole() != null) {
            profile.setLastRole(request.getLastRole());
        }

        if (request.getCurrentOrganisation() != null) {
            profile.setCurrentOrganisation(request.getCurrentOrganisation());
        }

        if (request.getCurrentRole() != null) {
            profile.setCurrentRole(request.getCurrentRole());
        }

        if (request.getLinkedInRef() != null) {
            profile.setLinkedInRef(request.getLinkedInRef());
        }

        if (request.getEngagementTier() != null) {
            profile.setEngagementTier(request.getEngagementTier());
        }

        if (request.getCommunityOptIn() != null) {
            profile.setCommunityOptIn(request.getCommunityOptIn());
        }

        if (request.getStatus() != null) {
            profile.setStatus(request.getStatus());
        }

        AlumniProfile updatedProfile = profileRepository.save(profile);

        return mapper.toResponse(updatedProfile);
    }

    @Override
    public AlumniProfileResponse deactivateProfile(Long alumniId) {
        AlumniProfile profile = findProfileById(alumniId);
        profile.setStatus(AlumniStatus.INACTIVE);

        AlumniProfile updatedProfile = profileRepository.save(profile);

        return mapper.toResponse(updatedProfile);
    }

    @Override
    public void deleteProfile(Long alumniId) {
        AlumniProfile profile = findProfileById(alumniId);
        profileRepository.delete(profile);
    }

    private AlumniProfile findProfileById(Long alumniId) {
        return profileRepository.findById(alumniId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni profile not found with ID: " + alumniId
                ));
    }
}