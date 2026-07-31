package com.aluminibackend.api.module.exitsurvey.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.exitsurvey.dto.request.SubmitExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.request.UpdateExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.response.ExitSurveyResponseDto;
import com.aluminibackend.api.module.exitsurvey.entity.ExitSurveyResponse;
import com.aluminibackend.api.module.exitsurvey.mapper.ExitSurveyMapper;
import com.aluminibackend.api.module.exitsurvey.repository.ExitSurveyResponseRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.DepartureType;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
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
public class ExitSurveyServiceImplTest {

    @Mock
    private ExitSurveyResponseRepository surveyRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExitSurveyMapper mapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ExitSurveyServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String name, UserRole role) {
        return User.builder()
                .userId(userId)
                .name(name)
                .role(role)
                .email(name.toLowerCase().replace(" ", "") + "@example.com")
                .employeeId("EMP" + userId)
                .password("password")
                .accountType(AccountType.ACTIVE)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private OffboardingCase createOffboardingCase(Long caseId, User employee, User hrbpOwner) {
        return OffboardingCase.builder()
                .caseId(caseId)
                .employee(employee)
                .departureType(DepartureType.RESIGNATION)
                .lastWorkingDate(LocalDate.now())
                .hrbpOwner(hrbpOwner)
                .status(OffboardingStatus.IN_PROGRESS)
                .build();
    }

    private ExitSurveyResponse createExitSurveyResponse(Long responseId, OffboardingCase offboardingCase, User employee, Integer mSatisfaction, Integer tCulture, Integer cGrowth, Integer compensation, Integer overall, String feedback, Boolean anonymised) {
        return ExitSurveyResponse.builder()
                .responseId(responseId)
                .offboardingCase(offboardingCase)
                .employee(employee)
                .submittedDate(LocalDateTime.now())
                .managerSatisfactionScore(mSatisfaction)
                .teamCultureScore(tCulture)
                .careerGrowthScore(cGrowth)
                .compensationScore(compensation)
                .overallScore(overall)
                .openTextFeedback(feedback)
                .anonymised(anonymised)
                .build();
    }

    private SubmitExitSurveyRequest createSubmitRequest(Long caseId, Long employeeId, Integer mSatisfaction, Integer tCulture, Integer cGrowth, Integer compensation, Integer overall, String feedback, Boolean anonymised) {
        SubmitExitSurveyRequest request = new SubmitExitSurveyRequest();
        request.setCaseId(caseId);
        request.setEmployeeId(employeeId);
        request.setManagerSatisfactionScore(mSatisfaction);
        request.setTeamCultureScore(tCulture);
        request.setCareerGrowthScore(cGrowth);
        request.setCompensationScore(compensation);
        request.setOverallScore(overall);
        request.setOpenTextFeedback(feedback);
        request.setAnonymised(anonymised);
        return request;
    }

    private UpdateExitSurveyRequest createUpdateRequest(Integer mSatisfaction, Integer tCulture, Integer cGrowth, Integer compensation, Integer overall, String feedback, Boolean anonymised) {
        UpdateExitSurveyRequest request = new UpdateExitSurveyRequest();
        request.setManagerSatisfactionScore(mSatisfaction);
        request.setTeamCultureScore(tCulture);
        request.setCareerGrowthScore(cGrowth);
        request.setCompensationScore(compensation);
        request.setOverallScore(overall);
        request.setOpenTextFeedback(feedback);
        request.setAnonymised(anonymised);
        return request;
    }

    private ExitSurveyResponseDto createResponseDto(Long responseId, Long caseId, Long employeeId, String employeeName, Integer mSatisfaction, Integer tCulture, Integer cGrowth, Integer compensation, Integer overall, String feedback, Boolean anonymised) {
        return ExitSurveyResponseDto.builder()
                .responseId(responseId)
                .caseId(caseId)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .submittedDate(LocalDateTime.now())
                .managerSatisfactionScore(mSatisfaction)
                .teamCultureScore(tCulture)
                .careerGrowthScore(cGrowth)
                .compensationScore(compensation)
                .overallScore(overall)
                .openTextFeedback(feedback)
                .anonymised(anonymised)
                .build();
    }

    // --- submitSurvey Tests ---

    @Test
    void submitSurvey_Success() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 10L, 5, 4, 3, 5, 4, "Good experience", true);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitSurveyResponse savedSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 4, 3, 5, 4, "Good experience", true);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 5, 4, 3, 5, 4, "Good experience", true);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(surveyRepository.save(any(ExitSurveyResponse.class))).thenReturn(savedSurvey);
        when(mapper.toResponse(savedSurvey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.submitSurvey(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<ExitSurveyResponse> captor = ArgumentCaptor.forClass(ExitSurveyResponse.class);
        verify(surveyRepository).save(captor.capture());
        ExitSurveyResponse captured = captor.getValue();
        assertEquals(offboardingCase, captured.getOffboardingCase());
        assertEquals(employee, captured.getEmployee());
        assertEquals(5, captured.getManagerSatisfactionScore());
        assertEquals(4, captured.getTeamCultureScore());
        assertEquals(3, captured.getCareerGrowthScore());
        assertEquals(5, captured.getCompensationScore());
        assertEquals(4, captured.getOverallScore());
        assertEquals("Good experience", captured.getOpenTextFeedback());
        assertTrue(captured.getAnonymised());

        verify(auditLogService).record("EMP10", "SUBMIT_EXIT_SURVEY", "EXIT_SURVEY");
        verify(surveyRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(10L);
        verify(mapper).toResponse(savedSurvey);
        verifyNoMoreInteractions(surveyRepository, offboardingCaseRepository, userRepository, mapper, auditLogService);
    }

    @Test
    void submitSurvey_Success_AnonymisedNull() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 10L, 5, 4, 3, 5, 4, "Good experience", null);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitSurveyResponse savedSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 4, 3, 5, 4, "Good experience", false);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 5, 4, 3, 5, 4, "Good experience", false);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(surveyRepository.save(any(ExitSurveyResponse.class))).thenReturn(savedSurvey);
        when(mapper.toResponse(savedSurvey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.submitSurvey(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<ExitSurveyResponse> captor = ArgumentCaptor.forClass(ExitSurveyResponse.class);
        verify(surveyRepository).save(captor.capture());
        ExitSurveyResponse captured = captor.getValue();
        assertFalse(captured.getAnonymised()); // Anonymised defaults to false when request field is null

        verify(auditLogService).record("EMP10", "SUBMIT_EXIT_SURVEY", "EXIT_SURVEY");
    }

    @Test
    void submitSurvey_ThrowsException_WhenSurveyAlreadySubmitted() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 10L, 5, 4, 3, 5, 4, "Good experience", true);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.submitSurvey(request);
        });

        assertEquals("Exit survey already submitted for this case", exception.getMessage());
        verify(surveyRepository).existsByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(offboardingCaseRepository, userRepository, mapper, auditLogService);
    }

    @Test
    void submitSurvey_ThrowsException_WhenCaseNotFound() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 10L, 5, 4, 3, 5, 4, "Good experience", true);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.submitSurvey(request);
        });

        assertEquals("Offboarding case not found", exception.getMessage());
        verify(surveyRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verifyNoMoreInteractions(surveyRepository, offboardingCaseRepository);
        verifyNoInteractions(userRepository, mapper, auditLogService);
    }

    @Test
    void submitSurvey_ThrowsException_WhenEmployeeNotFound() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 10L, 5, 4, 3, 5, 4, "Good experience", true);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.submitSurvey(request);
        });

        assertEquals("Employee not found", exception.getMessage());
        verify(surveyRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(10L);
        verifyNoMoreInteractions(surveyRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    @Test
    void submitSurvey_ThrowsException_WhenEmployeeMismatch() {
        SubmitExitSurveyRequest request = createSubmitRequest(1L, 11L, 5, 4, 3, 5, 4, "Good experience", true);
        User employeeInCase = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User employeeInRequest = createUser(11L, "Other Employee", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employeeInCase, hrbp);

        when(surveyRepository.existsByOffboardingCase_CaseId(1L)).thenReturn(false);
        when(offboardingCaseRepository.findById(1L)).thenReturn(Optional.of(offboardingCase));
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeInRequest));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.submitSurvey(request);
        });

        assertEquals("Employee does not belong to this offboarding case", exception.getMessage());
        verify(surveyRepository).existsByOffboardingCase_CaseId(1L);
        verify(offboardingCaseRepository).findById(1L);
        verify(userRepository).findById(11L);
        verifyNoMoreInteractions(surveyRepository, offboardingCaseRepository, userRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    // --- getSurveyById Tests ---

    @Test
    void getSurveyById_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse survey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", false);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Feedback", false);

        when(surveyRepository.findById(100L)).thenReturn(Optional.of(survey));
        when(mapper.toResponse(survey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.getSurveyById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(surveyRepository).findById(100L);
        verify(mapper).toResponse(survey);
        verifyNoMoreInteractions(surveyRepository, mapper);
    }

    @Test
    void getSurveyById_ThrowsException_WhenNotFound() {
        when(surveyRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getSurveyById(100L);
        });

        assertEquals("Exit survey not found with ID: 100", exception.getMessage());
        verify(surveyRepository).findById(100L);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper);
    }

    // --- getSurveyByCase Tests ---

    @Test
    void getSurveyByCase_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse survey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", false);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Feedback", false);

        when(surveyRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.of(survey));
        when(mapper.toResponse(survey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.getSurveyByCase(1L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(surveyRepository).findByOffboardingCase_CaseId(1L);
        verify(mapper).toResponse(survey);
        verifyNoMoreInteractions(surveyRepository, mapper);
    }

    @Test
    void getSurveyByCase_ThrowsException_WhenNotFound() {
        when(surveyRepository.findByOffboardingCase_CaseId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getSurveyByCase(1L);
        });

        assertEquals("Exit survey not found for case ID: 1", exception.getMessage());
        verify(surveyRepository).findByOffboardingCase_CaseId(1L);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper);
    }

    // --- getAllSurveys Tests ---

    @Test
    void getAllSurveys_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse survey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", false);
        ExitSurveyResponseDto responseDto = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Feedback", false);

        when(surveyRepository.findAll()).thenReturn(Collections.singletonList(survey));
        when(mapper.toResponse(survey)).thenReturn(responseDto);

        List<ExitSurveyResponseDto> results = service.getAllSurveys();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(surveyRepository).findAll();
        verify(mapper).toResponse(survey);
        verifyNoMoreInteractions(surveyRepository, mapper);
    }

    @Test
    void getAllSurveys_Empty() {
        when(surveyRepository.findAll()).thenReturn(Collections.emptyList());

        List<ExitSurveyResponseDto> results = service.getAllSurveys();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(surveyRepository).findAll();
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper);
    }

    // --- getSurveysByEmployee Tests ---

    @Test
    void getSurveysByEmployee_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse survey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", false);
        ExitSurveyResponseDto responseDto = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Feedback", false);

        when(surveyRepository.findByEmployee_UserId(10L)).thenReturn(Collections.singletonList(survey));
        when(mapper.toResponse(survey)).thenReturn(responseDto);

        List<ExitSurveyResponseDto> results = service.getSurveysByEmployee(10L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(surveyRepository).findByEmployee_UserId(10L);
        verify(mapper).toResponse(survey);
        verifyNoMoreInteractions(surveyRepository, mapper);
    }

    @Test
    void getSurveysByEmployee_Empty() {
        when(surveyRepository.findByEmployee_UserId(10L)).thenReturn(Collections.emptyList());

        List<ExitSurveyResponseDto> results = service.getSurveysByEmployee(10L);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(surveyRepository).findByEmployee_UserId(10L);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper);
    }

    // --- getAnonymousSurveys Tests ---

    @Test
    void getAnonymousSurveys_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse survey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", true);
        ExitSurveyResponseDto responseDto = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Feedback", true);

        when(surveyRepository.findByAnonymised(true)).thenReturn(Collections.singletonList(survey));
        when(mapper.toResponse(survey)).thenReturn(responseDto);

        List<ExitSurveyResponseDto> results = service.getAnonymousSurveys();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(responseDto, results.get(0));
        verify(surveyRepository).findByAnonymised(true);
        verify(mapper).toResponse(survey);
        verifyNoMoreInteractions(surveyRepository, mapper);
    }

    @Test
    void getAnonymousSurveys_Empty() {
        when(surveyRepository.findByAnonymised(true)).thenReturn(Collections.emptyList());

        List<ExitSurveyResponseDto> results = service.getAnonymousSurveys();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(surveyRepository).findByAnonymised(true);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper);
    }

    // --- updateSurvey Tests ---

    @Test
    void updateSurvey_Success_AllFieldsChanged() {
        UpdateExitSurveyRequest request = createUpdateRequest(1, 2, 3, 4, 5, "New feedback", true);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitSurveyResponse existingSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Old feedback", false);
        ExitSurveyResponse updatedSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 1, 2, 3, 4, 5, "New feedback", true);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 1, 2, 3, 4, 5, "New feedback", true);

        when(surveyRepository.findById(100L)).thenReturn(Optional.of(existingSurvey));
        when(surveyRepository.save(existingSurvey)).thenReturn(updatedSurvey);
        when(mapper.toResponse(updatedSurvey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.updateSurvey(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(1, existingSurvey.getManagerSatisfactionScore());
        assertEquals(2, existingSurvey.getTeamCultureScore());
        assertEquals(3, existingSurvey.getCareerGrowthScore());
        assertEquals(4, existingSurvey.getCompensationScore());
        assertEquals(5, existingSurvey.getOverallScore());
        assertEquals("New feedback", existingSurvey.getOpenTextFeedback());
        assertTrue(existingSurvey.getAnonymised());

        verify(surveyRepository).findById(100L);
        verify(surveyRepository).save(existingSurvey);
        verify(auditLogService).record("EMP10", "UPDATE_EXIT_SURVEY", "EXIT_SURVEY");
        verify(mapper).toResponse(updatedSurvey);
        verifyNoMoreInteractions(surveyRepository, mapper, auditLogService);
    }

    @Test
    void updateSurvey_Success_NullFields() {
        UpdateExitSurveyRequest request = createUpdateRequest(null, null, null, null, null, null, null);
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);

        ExitSurveyResponse existingSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Old feedback", false);
        ExitSurveyResponseDto expectedResponse = createResponseDto(100L, 1L, 10L, "Employee User", 5, 5, 5, 5, 5, "Old feedback", false);

        when(surveyRepository.findById(100L)).thenReturn(Optional.of(existingSurvey));
        when(surveyRepository.save(existingSurvey)).thenReturn(existingSurvey);
        when(mapper.toResponse(existingSurvey)).thenReturn(expectedResponse);

        ExitSurveyResponseDto response = service.updateSurvey(100L, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(5, existingSurvey.getManagerSatisfactionScore());
        assertEquals("Old feedback", existingSurvey.getOpenTextFeedback());
        assertFalse(existingSurvey.getAnonymised());

        verify(surveyRepository).findById(100L);
        verify(surveyRepository).save(existingSurvey);
        verify(auditLogService).record("EMP10", "UPDATE_EXIT_SURVEY", "EXIT_SURVEY");
        verify(mapper).toResponse(existingSurvey);
        verifyNoMoreInteractions(surveyRepository, mapper, auditLogService);
    }

    @Test
    void updateSurvey_ThrowsException_WhenNotFound() {
        UpdateExitSurveyRequest request = createUpdateRequest(1, 2, 3, 4, 5, "New feedback", true);

        when(surveyRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateSurvey(100L, request);
        });

        assertEquals("Exit survey not found with ID: 100", exception.getMessage());
        verify(surveyRepository).findById(100L);
        verifyNoMoreInteractions(surveyRepository);
        verifyNoInteractions(mapper, auditLogService);
    }

    // --- deleteSurvey Tests ---

    @Test
    void deleteSurvey_Success() {
        User employee = createUser(10L, "Employee User", UserRole.EMPLOYEE);
        User hrbp = createUser(2L, "HRBP User", UserRole.HRBP);
        OffboardingCase offboardingCase = createOffboardingCase(1L, employee, hrbp);
        ExitSurveyResponse existingSurvey = createExitSurveyResponse(100L, offboardingCase, employee, 5, 5, 5, 5, 5, "Feedback", false);

        when(surveyRepository.findById(100L)).thenReturn(Optional.of(existingSurvey));
        doNothing().when(surveyRepository).delete(existingSurvey);

        service.deleteSurvey(100L);

        verify(surveyRepository).findById(100L);
        verify(surveyRepository).delete(existingSurvey);
        verifyNoMoreInteractions(surveyRepository);
    }

    @Test
    void deleteSurvey_ThrowsException_WhenNotFound() {
        when(surveyRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteSurvey(100L);
        });

        assertEquals("Exit survey not found with ID: 100", exception.getMessage());
        verify(surveyRepository).findById(100L);
        verifyNoMoreInteractions(surveyRepository);
    }
}
