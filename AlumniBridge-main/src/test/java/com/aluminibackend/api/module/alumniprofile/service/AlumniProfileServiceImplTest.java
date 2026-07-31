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
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlumniProfileServiceImplTest {

    @Mock
    private AlumniProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlumniProfileMapper mapper;

    @InjectMocks
    private AlumniProfileServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name, String employeeId, String email) {
        return User.builder()
                .userId(userId)
                .name(name)
                .employeeId(employeeId)
                .email(email)
                .role(UserRole.EMPLOYEE)
                .password("password")
                .accountType(AccountType.ACTIVE)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AlumniProfile createProfileEntity(Long alumniId, User user, EngagementTier engagementTier, Boolean optIn, AlumniStatus status) {
        return AlumniProfile.builder()
                .alumniId(alumniId)
                .user(user)
                .formerEmployeeId(user.getEmployeeId())
                .name(user.getName())
                .lastRole("Software Engineer")
                .departureDateFrom(LocalDate.now().minusMonths(6))
                .currentOrganisation("New Tech Corp")
                .currentRole("Senior Software Engineer")
                .linkedInRef("https://linkedin.com/in/test")
                .engagementTier(engagementTier)
                .communityOptIn(optIn)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateAlumniProfileRequest createCreateRequest(String employeeId, EngagementTier engagementTier, Boolean optIn) {
        CreateAlumniProfileRequest request = new CreateAlumniProfileRequest();
        request.setEmployeeId(employeeId);
        request.setLastRole("Software Engineer");
        request.setDepartureDateFrom(LocalDate.now().minusMonths(6));
        request.setCurrentOrganisation("New Tech Corp");
        request.setCurrentRole("Senior Software Engineer");
        request.setLinkedInRef("https://linkedin.com/in/test");
        request.setEngagementTier(engagementTier);
        request.setCommunityOptIn(optIn);
        return request;
    }

    private UpdateAlumniProfileRequest createUpdateRequest(String lastRole, String currentOrg, String currentRole, String linkedin, EngagementTier engagementTier, Boolean optIn, AlumniStatus status) {
        UpdateAlumniProfileRequest request = new UpdateAlumniProfileRequest();
        request.setLastRole(lastRole);
        request.setCurrentOrganisation(currentOrg);
        request.setCurrentRole(currentRole);
        request.setLinkedInRef(linkedin);
        request.setEngagementTier(engagementTier);
        request.setCommunityOptIn(optIn);
        request.setStatus(status);
        return request;
    }

    private AlumniProfileResponse createResponse(Long alumniId, Long userId, String formerEmpId, String name, String email, EngagementTier engagementTier, Boolean optIn, AlumniStatus status) {
        return AlumniProfileResponse.builder()
                .alumniId(alumniId)
                .employeeId(formerEmpId)
                .formerEmployeeId(formerEmpId)
                .name(name)
                .email(email)
                .lastRole("Software Engineer")
                .departureDateFrom(LocalDate.now().minusMonths(6))
                .currentOrganisation("New Tech Corp")
                .currentRole("Senior Software Engineer")
                .linkedInRef("https://linkedin.com/in/test")
                .engagementTier(engagementTier)
                .communityOptIn(optIn)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- createProfile Tests ---

    @Test
    void createProfile_Success_WithOptionalFields() {
        CreateAlumniProfileRequest request = createCreateRequest("EMP001", EngagementTier.ENGAGED, true);
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile savedProfile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.existsByUser_EmployeeId("EMP001")).thenReturn(false);
        when(userRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(profileRepository.save(any(AlumniProfile.class))).thenReturn(savedProfile);
        when(mapper.toResponse(savedProfile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.createProfile(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        // Verify User updates
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals(AccountType.ALUMNI, updatedUser.getAccountType());
        assertEquals(UserStatus.ALUMNI, updatedUser.getStatus());

        // Verify AlumniProfile fields
        ArgumentCaptor<AlumniProfile> profileCaptor = ArgumentCaptor.forClass(AlumniProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        AlumniProfile profileToSave = profileCaptor.getValue();
        assertEquals(user, profileToSave.getUser());
        assertEquals("EMP001", profileToSave.getFormerEmployeeId());
        assertEquals("John Doe", profileToSave.getName());
        assertEquals("Software Engineer", profileToSave.getLastRole());
        assertEquals(EngagementTier.ENGAGED, profileToSave.getEngagementTier());
        assertTrue(profileToSave.getCommunityOptIn());
        assertEquals(AlumniStatus.ACTIVE, profileToSave.getStatus());

        verify(profileRepository).existsByUser_EmployeeId("EMP001");
        verify(userRepository).findById(1L);
        verify(mapper).toResponse(savedProfile);

        verifyNoMoreInteractions(profileRepository, userRepository, mapper);
    }

    @Test
    void createProfile_Success_WithDefaultFields() {
        // request with null values for optional fields (engagementTier and communityOptIn)
        CreateAlumniProfileRequest request = createCreateRequest("EMP001", null, null);
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile savedProfile = createProfileEntity(100L, user, EngagementTier.PASSIVE, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.PASSIVE, true, AlumniStatus.ACTIVE);

        when(profileRepository.existsByUser_EmployeeId("EMP001")).thenReturn(false);
        when(userRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(profileRepository.save(any(AlumniProfile.class))).thenReturn(savedProfile);
        when(mapper.toResponse(savedProfile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.createProfile(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniProfile> profileCaptor = ArgumentCaptor.forClass(AlumniProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        AlumniProfile profileToSave = profileCaptor.getValue();
        assertEquals(EngagementTier.PASSIVE, profileToSave.getEngagementTier());
        assertTrue(profileToSave.getCommunityOptIn());

        verify(profileRepository).existsByUser_EmployeeId("EMP001");
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(mapper).toResponse(savedProfile);
    }

    @Test
    void createProfile_ThrowsException_WhenProfileAlreadyExists() {
        CreateAlumniProfileRequest request = createCreateRequest("EMP001", EngagementTier.ENGAGED, true);

        when(profileRepository.existsByUser_EmployeeId("EMP001")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createProfile(request);
        });

        assertEquals("Alumni profile already exists for this user", exception.getMessage());
        verify(profileRepository).existsByUser_EmployeeId("EMP001");
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(userRepository, mapper);
    }

    @Test
    void createProfile_ThrowsException_WhenUserNotFound() {
        CreateAlumniProfileRequest request = createCreateRequest("EMP001", EngagementTier.ENGAGED, true);

        when(profileRepository.existsByUser_EmployeeId("EMP001")).thenReturn(false);
        when(userRepository.findByEmployeeId("EMP001")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createProfile(request);
        });

        assertEquals("User not found", exception.getMessage());
        verify(profileRepository).existsByUser_EmployeeId("EMP001");
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(profileRepository, userRepository);
        verifyNoInteractions(mapper);
    }

    // --- getProfileById Tests ---

    @Test
    void getProfileById_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(mapper.toResponse(profile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.getProfileById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileRepository).findById(100L);
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getProfileById_ThrowsException_WhenNotFound() {
        when(profileRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getProfileById(100L);
        });

        assertEquals("Alumni profile not found with ID: 100", exception.getMessage());
        verify(profileRepository).findById(100L);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getProfileByUser Tests ---

    @Test
    void getProfileByUser_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findByUser_EmployeeId("EMP001")).thenReturn(Optional.of(profile));
        when(mapper.toResponse(profile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.getProfileByUser("EMP001");

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileRepository).findByUser_EmployeeId("EMP001");
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getProfileByUser_ThrowsException_WhenNotFound() {
        when(profileRepository.findByUser_EmployeeId("EMP001")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getProfileByUser("EMP001");
        });

        assertEquals("Alumni profile not found for employee ID: EMP001", exception.getMessage());
        verify(profileRepository).findByUser_EmployeeId("EMP001");
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getProfileByFormerEmployeeId Tests ---

    @Test
    void getProfileByFormerEmployeeId_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findByFormerEmployeeId("EMP001")).thenReturn(Optional.of(profile));
        when(mapper.toResponse(profile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.getProfileByFormerEmployeeId("EMP001");

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileRepository).findByFormerEmployeeId("EMP001");
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getProfileByFormerEmployeeId_ThrowsException_WhenNotFound() {
        when(profileRepository.findByFormerEmployeeId("EMP001")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getProfileByFormerEmployeeId("EMP001");
        });

        assertEquals("Alumni profile not found for employee ID: EMP001", exception.getMessage());
        verify(profileRepository).findByFormerEmployeeId("EMP001");
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getAllProfiles Tests ---

    @Test
    void getAllProfiles_Success() {
        User user1 = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        User user2 = createUser(2L, "Jane Doe", "EMP002", "jane.doe@example.com");
        AlumniProfile profile1 = createProfileEntity(100L, user1, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfile profile2 = createProfileEntity(200L, user2, EngagementTier.PASSIVE, false, AlumniStatus.ACTIVE);
        AlumniProfileResponse resp1 = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse resp2 = createResponse(200L, 2L, "EMP002", "Jane Doe", "jane.doe@example.com", EngagementTier.PASSIVE, false, AlumniStatus.ACTIVE);

        when(profileRepository.findAll()).thenReturn(Arrays.asList(profile1, profile2));
        when(mapper.toResponse(profile1)).thenReturn(resp1);
        when(mapper.toResponse(profile2)).thenReturn(resp2);

        List<AlumniProfileResponse> responses = service.getAllProfiles();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(resp1, responses.get(0));
        assertEquals(resp2, responses.get(1));

        verify(profileRepository).findAll();
        verify(mapper).toResponse(profile1);
        verify(mapper).toResponse(profile2);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getAllProfiles_Empty() {
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());

        List<AlumniProfileResponse> responses = service.getAllProfiles();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(profileRepository).findAll();
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getProfilesByEngagementTier Tests ---

    @Test
    void getProfilesByEngagementTier_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse resp = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findByEngagementTier(EngagementTier.ENGAGED)).thenReturn(Collections.singletonList(profile));
        when(mapper.toResponse(profile)).thenReturn(resp);

        List<AlumniProfileResponse> responses = service.getProfilesByEngagementTier(EngagementTier.ENGAGED);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(resp, responses.get(0));

        verify(profileRepository).findByEngagementTier(EngagementTier.ENGAGED);
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getProfilesByEngagementTier_Empty() {
        when(profileRepository.findByEngagementTier(EngagementTier.ENGAGED)).thenReturn(Collections.emptyList());

        List<AlumniProfileResponse> responses = service.getProfilesByEngagementTier(EngagementTier.ENGAGED);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(profileRepository).findByEngagementTier(EngagementTier.ENGAGED);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getProfilesByStatus Tests ---

    @Test
    void getProfilesByStatus_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse resp = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findByStatus(AlumniStatus.ACTIVE)).thenReturn(Collections.singletonList(profile));
        when(mapper.toResponse(profile)).thenReturn(resp);

        List<AlumniProfileResponse> responses = service.getProfilesByStatus(AlumniStatus.ACTIVE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(resp, responses.get(0));

        verify(profileRepository).findByStatus(AlumniStatus.ACTIVE);
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getProfilesByStatus_Empty() {
        when(profileRepository.findByStatus(AlumniStatus.ACTIVE)).thenReturn(Collections.emptyList());

        List<AlumniProfileResponse> responses = service.getProfilesByStatus(AlumniStatus.ACTIVE);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(profileRepository).findByStatus(AlumniStatus.ACTIVE);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- getCommunityOptInProfiles Tests ---

    @Test
    void getCommunityOptInProfiles_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse resp = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findByCommunityOptInIs(true)).thenReturn(Collections.singletonList(profile));
        when(mapper.toResponse(profile)).thenReturn(resp);

        List<AlumniProfileResponse> responses = service.getCommunityOptInProfiles();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(resp, responses.get(0));

        verify(profileRepository).findByCommunityOptInIs(true);
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void getCommunityOptInProfiles_Empty() {
        when(profileRepository.findByCommunityOptInIs(true)).thenReturn(Collections.emptyList());

        List<AlumniProfileResponse> responses = service.getCommunityOptInProfiles();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(profileRepository).findByCommunityOptInIs(true);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- updateProfile Tests ---

    @Test
    void updateProfile_Success_AllFields() {
        UpdateAlumniProfileRequest request = createUpdateRequest(
                "Staff Software Engineer",
                "Even Newer Tech",
                "Lead Engineer",
                "https://linkedin.com/in/updated",
                EngagementTier.LAPSED,
                false,
                AlumniStatus.INACTIVE
        );

        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfile updatedProfile = createProfileEntity(100L, user, EngagementTier.LAPSED, false, AlumniStatus.INACTIVE);
        updatedProfile.setLastRole("Staff Software Engineer");
        updatedProfile.setCurrentOrganisation("Even Newer Tech");
        updatedProfile.setCurrentRole("Lead Engineer");
        updatedProfile.setLinkedInRef("https://linkedin.com/in/updated");

        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.LAPSED, false, AlumniStatus.INACTIVE);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(AlumniProfile.class))).thenReturn(updatedProfile);
        when(mapper.toResponse(updatedProfile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.updateProfile(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniProfile> profileCaptor = ArgumentCaptor.forClass(AlumniProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        AlumniProfile savedProfile = profileCaptor.getValue();
        assertEquals("Staff Software Engineer", savedProfile.getLastRole());
        assertEquals("Even Newer Tech", savedProfile.getCurrentOrganisation());
        assertEquals("Lead Engineer", savedProfile.getCurrentRole());
        assertEquals("https://linkedin.com/in/updated", savedProfile.getLinkedInRef());
        assertEquals(EngagementTier.LAPSED, savedProfile.getEngagementTier());
        assertFalse(savedProfile.getCommunityOptIn());
        assertEquals(AlumniStatus.INACTIVE, savedProfile.getStatus());

        verify(profileRepository).findById(100L);
        verify(mapper).toResponse(updatedProfile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void updateProfile_Success_NoFields() {
        // Request with all null values - should keep current values on the profile
        UpdateAlumniProfileRequest request = createUpdateRequest(
                null, null, null, null, null, null, null
        );

        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(AlumniProfile.class))).thenReturn(profile);
        when(mapper.toResponse(profile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.updateProfile(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniProfile> profileCaptor = ArgumentCaptor.forClass(AlumniProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        AlumniProfile savedProfile = profileCaptor.getValue();
        // Should be unchanged
        assertEquals("Software Engineer", savedProfile.getLastRole());
        assertEquals("New Tech Corp", savedProfile.getCurrentOrganisation());
        assertEquals("Senior Software Engineer", savedProfile.getCurrentRole());
        assertEquals("https://linkedin.com/in/test", savedProfile.getLinkedInRef());
        assertEquals(EngagementTier.ENGAGED, savedProfile.getEngagementTier());
        assertTrue(savedProfile.getCommunityOptIn());
        assertEquals(AlumniStatus.ACTIVE, savedProfile.getStatus());

        verify(profileRepository).findById(100L);
        verify(mapper).toResponse(profile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void updateProfile_ThrowsException_WhenNotFound() {
        UpdateAlumniProfileRequest request = createUpdateRequest(
                "Role", "Org", "Role", "Lnk", EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE
        );

        when(profileRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateProfile(100L, request);
        });

        assertEquals("Alumni profile not found with ID: 100", exception.getMessage());
        verify(profileRepository).findById(100L);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- deactivateProfile Tests ---

    @Test
    void deactivateProfile_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);
        AlumniProfile deactivatedProfile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.INACTIVE);
        AlumniProfileResponse expectedResponse = createResponse(100L, 1L, "EMP001", "John Doe", "john.doe@example.com", EngagementTier.ENGAGED, true, AlumniStatus.INACTIVE);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(AlumniProfile.class))).thenReturn(deactivatedProfile);
        when(mapper.toResponse(deactivatedProfile)).thenReturn(expectedResponse);

        AlumniProfileResponse response = service.deactivateProfile(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniProfile> profileCaptor = ArgumentCaptor.forClass(AlumniProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertEquals(AlumniStatus.INACTIVE, profileCaptor.getValue().getStatus());

        verify(profileRepository).findById(100L);
        verify(mapper).toResponse(deactivatedProfile);
        verifyNoMoreInteractions(profileRepository, mapper);
    }

    @Test
    void deactivateProfile_ThrowsException_WhenNotFound() {
        when(profileRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deactivateProfile(100L);
        });

        assertEquals("Alumni profile not found with ID: 100", exception.getMessage());
        verify(profileRepository).findById(100L);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    // --- deleteProfile Tests ---

    @Test
    void deleteProfile_Success() {
        User user = createUser(1L, "John Doe", "EMP001", "john.doe@example.com");
        AlumniProfile profile = createProfileEntity(100L, user, EngagementTier.ENGAGED, true, AlumniStatus.ACTIVE);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        doNothing().when(profileRepository).delete(profile);

        service.deleteProfile(100L);

        verify(profileRepository).findById(100L);
        verify(profileRepository).delete(profile);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteProfile_ThrowsException_WhenNotFound() {
        when(profileRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteProfile(100L);
        });

        assertEquals("Alumni profile not found with ID: 100", exception.getMessage());
        verify(profileRepository).findById(100L);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(mapper);
    }
}
