package com.aluminibackend.api.module.alumnireport.service;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import com.aluminibackend.api.module.alumniprofile.repository.AlumniProfileRepository;
import com.aluminibackend.api.module.alumnireport.dto.request.CreateAlumniReportRequest;
import com.aluminibackend.api.module.alumnireport.dto.response.AlumniReportResponse;
import com.aluminibackend.api.module.alumnireport.entity.AlumniReport;
import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import com.aluminibackend.api.module.alumnireport.mapper.AlumniReportMapper;
import com.aluminibackend.api.module.alumnireport.repository.AlumniReportRepository;
import com.aluminibackend.api.module.alumnireferral.enums.AlumniReferralStatus;
import com.aluminibackend.api.module.alumnireferral.repository.AlumniReferralRepository;
import com.aluminibackend.api.module.boomerangapplication.entity.BoomerangApplication;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import com.aluminibackend.api.module.boomerangapplication.repository.BoomerangApplicationRepository;
import com.aluminibackend.api.module.exitinterview.entity.ExitInterview;
import com.aluminibackend.api.module.exitinterview.repository.ExitInterviewRepository;
import com.aluminibackend.api.module.exitsurvey.entity.ExitSurveyResponse;
import com.aluminibackend.api.module.exitsurvey.repository.ExitSurveyResponseRepository;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlumniReportServiceImplTest {

    @Mock
    private AlumniReportRepository reportRepository;

    @Mock
    private AlumniReportMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OffboardingCaseRepository offboardingCaseRepository;

    @Mock
    private ExitSurveyResponseRepository exitSurveyRepository;

    @Mock
    private ExitInterviewRepository exitInterviewRepository;

    @Mock
    private AlumniProfileRepository alumniProfileRepository;

    @Mock
    private BoomerangApplicationRepository boomerangApplicationRepository;

    @Mock
    private AlumniReferralRepository alumniReferralRepository;

    @InjectMocks
    private AlumniReportServiceImpl service;

    // --- Helper Methods ---

    private AlumniReport createAlumniReport(Long reportId, ReportScope scope, ReportMetric metric, String metricValue) {
        return AlumniReport.builder()
                .reportId(reportId)
                .scope(scope)
                .metric(metric)
                .metricValue(metricValue)
                .generatedDate(LocalDateTime.now())
                .build();
    }

    private AlumniReportResponse createAlumniReportResponse(Long reportId, ReportScope scope, ReportMetric metric,
            String metricValue) {
        return AlumniReportResponse.builder()
                .reportId(reportId)
                .scope(scope)
                .metric(metric)
                .metricValue(metricValue)
                .generatedDate(LocalDateTime.now())
                .build();
    }

    private CreateAlumniReportRequest createCreateRequest(ReportScope scope, ReportMetric metric, String metricValue) {
        CreateAlumniReportRequest request = new CreateAlumniReportRequest();
        request.setScope(scope);
        request.setMetric(metric);
        request.setMetricValue(metricValue);
        return request;
    }

    private BoomerangApplication createBoomerangApplication(Double tenure) {
        return BoomerangApplication.builder()
                .previousTenureYears(tenure)
                .build();
    }

    private ExitSurveyResponse createExitSurveyResponse(Integer score) {
        return ExitSurveyResponse.builder()
                .overallScore(score)
                .build();
    }

    private ExitInterview createExitInterview(String reason) {
        return ExitInterview.builder()
                .primaryReasonForLeaving(reason)
                .build();
    }

    private AlumniProfile createAlumniProfile(EngagementTier tier, Boolean communityOptIn) {
        return AlumniProfile.builder()
                .engagementTier(tier)
                .communityOptIn(communityOptIn)
                .build();
    }

    // --- createReport Tests ---

    @Test
    void createReport_Success_WithExplicitMetricValue() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.DEPARTMENT, ReportMetric.ATTRITION_RATE,
                "Explicit Value");
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.DEPARTMENT, ReportMetric.ATTRITION_RATE,
                "Explicit Value");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.DEPARTMENT,
                ReportMetric.ATTRITION_RATE, "Explicit Value");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        AlumniReport captured = reportCaptor.getValue();
        assertEquals(ReportScope.DEPARTMENT, captured.getScope());
        assertEquals(ReportMetric.ATTRITION_RATE, captured.getMetric());
        assertEquals("Explicit Value", captured.getMetricValue());

        verify(mapper).toResponse(savedReport);
    }

    @Test
    void createReport_Success_ResolveAttritionRate_TotalUsersGreaterThanZero() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.GRADE, ReportMetric.ATTRITION_RATE, null);
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE,
                "10.00% (1 offboarding cases / 10 users)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.GRADE,
                ReportMetric.ATTRITION_RATE, "10.00% (1 offboarding cases / 10 users)");

        when(userRepository.count()).thenReturn(10L);
        when(offboardingCaseRepository.count()).thenReturn(1L);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("10.00% (1 offboarding cases / 10 users)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveAttritionRate_TotalUsersZero() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "");
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE,
                "0.00% (0 offboarding cases / 0 users)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.GRADE,
                ReportMetric.ATTRITION_RATE, "0.00% (0 offboarding cases / 0 users)");

        when(userRepository.count()).thenReturn(0L);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("0.00% (0 offboarding cases / 0 users)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveAvgTenure_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD, ReportMetric.AVG_TENURE, null);
        List<BoomerangApplication> apps = Arrays.asList(
                createBoomerangApplication(3.0),
                createBoomerangApplication(null),
                createBoomerangApplication(4.0));
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.AVG_TENURE, "3.50 years");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.AVG_TENURE, "3.50 years");

        when(boomerangApplicationRepository.findAll()).thenReturn(apps);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("3.50 years", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveAvgTenure_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD, ReportMetric.AVG_TENURE, null);
        when(boomerangApplicationRepository.findAll()).thenReturn(Collections.emptyList());
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.AVG_TENURE,
                "Not available: previous tenure years have not been captured");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.AVG_TENURE, "Not available: previous tenure years have not been captured");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("Not available: previous tenure years have not been captured",
                reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveExitSurveyScore_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.DEPARTURE_TYPE,
                ReportMetric.EXIT_SURVEY_SCORE, null);
        List<ExitSurveyResponse> responses = Arrays.asList(
                createExitSurveyResponse(4),
                createExitSurveyResponse(null),
                createExitSurveyResponse(5));
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.DEPARTURE_TYPE, ReportMetric.EXIT_SURVEY_SCORE,
                "4.50 / 5");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.DEPARTURE_TYPE,
                ReportMetric.EXIT_SURVEY_SCORE, "4.50 / 5");

        when(exitSurveyRepository.findAll()).thenReturn(responses);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("4.50 / 5", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveExitSurveyScore_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.DEPARTURE_TYPE,
                ReportMetric.EXIT_SURVEY_SCORE, null);
        when(exitSurveyRepository.findAll()).thenReturn(Collections.emptyList());
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.DEPARTURE_TYPE, ReportMetric.EXIT_SURVEY_SCORE,
                "Not available: no exit survey scores submitted");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.DEPARTURE_TYPE,
                ReportMetric.EXIT_SURVEY_SCORE, "Not available: no exit survey scores submitted");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("Not available: no exit survey scores submitted", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveTopExitReasons_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.DEPARTMENT, ReportMetric.TOP_EXIT_REASONS,
                null);
        List<ExitInterview> interviews = Arrays.asList(
                createExitInterview("Better Career Opportunities"),
                createExitInterview("Better Career Opportunities"),
                createExitInterview("Relocation"),
                createExitInterview("Better Career Opportunities"),
                createExitInterview("Relocation"),
                createExitInterview(""),
                createExitInterview(null),
                createExitInterview("Compensation"));
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.DEPARTMENT, ReportMetric.TOP_EXIT_REASONS,
                "Better Career Opportunities: 3, Relocation: 2, Compensation: 1");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.DEPARTMENT,
                ReportMetric.TOP_EXIT_REASONS, "Better Career Opportunities: 3, Relocation: 2, Compensation: 1");

        when(exitInterviewRepository.findAll()).thenReturn(interviews);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        // Verify output sorting (Better Career Opportunities -> Relocation ->
        // Compensation)
        String val = reportCaptor.getValue().getMetricValue();
        assertTrue(val.contains("Better Career Opportunities: 3"));
        assertTrue(val.contains("Relocation: 2"));
        assertTrue(val.contains("Compensation: 1"));
    }

    @Test
    void createReport_Success_ResolveTopExitReasons_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.DEPARTMENT, ReportMetric.TOP_EXIT_REASONS,
                null);
        when(exitInterviewRepository.findAll()).thenReturn(Collections.emptyList());
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.DEPARTMENT, ReportMetric.TOP_EXIT_REASONS,
                "Not available: no exit interview reasons captured");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.DEPARTMENT,
                ReportMetric.TOP_EXIT_REASONS, "Not available: no exit interview reasons captured");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("Not available: no exit interview reasons captured", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveAlumniEngagementRate_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.GRADE, ReportMetric.ALUMNI_ENGAGEMENT_RATE,
                null);
        List<AlumniProfile> profiles = Arrays.asList(
                createAlumniProfile(EngagementTier.ENGAGED, false),
                createAlumniProfile(EngagementTier.LAPSED, true),
                createAlumniProfile(EngagementTier.PASSIVE, false),
                createAlumniProfile(EngagementTier.ENGAGED, true));
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ALUMNI_ENGAGEMENT_RATE,
                "75.00% (3 engaged alumni / 4 alumni profiles)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.GRADE,
                ReportMetric.ALUMNI_ENGAGEMENT_RATE, "75.00% (3 engaged alumni / 4 alumni profiles)");

        when(alumniProfileRepository.findAll()).thenReturn(profiles);
        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("75.00% (3 engaged alumni / 4 alumni profiles)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveAlumniEngagementRate_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.GRADE, ReportMetric.ALUMNI_ENGAGEMENT_RATE,
                null);
        when(alumniProfileRepository.findAll()).thenReturn(Collections.emptyList());
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ALUMNI_ENGAGEMENT_RATE,
                "0.00% (0 engaged alumni / 0 alumni profiles)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.GRADE,
                ReportMetric.ALUMNI_ENGAGEMENT_RATE, "0.00% (0 engaged alumni / 0 alumni profiles)");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("0.00% (0 engaged alumni / 0 alumni profiles)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveBoomerangHireRate_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD, ReportMetric.BOOMERANG_HIRE_RATE,
                null);
        List<BoomerangApplication> joinedApps = Arrays.asList(new BoomerangApplication(), new BoomerangApplication());

        when(boomerangApplicationRepository.count()).thenReturn(10L);
        when(boomerangApplicationRepository.findByStatus(BoomerangApplicationStatus.JOINED)).thenReturn(joinedApps);
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.BOOMERANG_HIRE_RATE,
                "20.00% (2 joined / 10 boomerang applications)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.BOOMERANG_HIRE_RATE, "20.00% (2 joined / 10 boomerang applications)");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("20.00% (2 joined / 10 boomerang applications)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveBoomerangHireRate_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD, ReportMetric.BOOMERANG_HIRE_RATE,
                null);
        when(boomerangApplicationRepository.count()).thenReturn(0L);
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.BOOMERANG_HIRE_RATE,
                "0.00% (0 joined / 0 boomerang applications)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.BOOMERANG_HIRE_RATE, "0.00% (0 joined / 0 boomerang applications)");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("0.00% (0 joined / 0 boomerang applications)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveReferralConversionRate_WithData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD,
                ReportMetric.REFERRAL_CONVERSION_RATE, null);

        when(alumniReferralRepository.count()).thenReturn(5L);
        when(alumniReferralRepository.findByStatus(AlumniReferralStatus.HIRED)).thenReturn(Arrays.asList(null, null));
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.REFERRAL_CONVERSION_RATE,
                "40.00% (2 hired / 5 referrals)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.REFERRAL_CONVERSION_RATE, "40.00% (2 hired / 5 referrals)");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("40.00% (2 hired / 5 referrals)", reportCaptor.getValue().getMetricValue());
    }

    @Test
    void createReport_Success_ResolveReferralConversionRate_EmptyData() {
        CreateAlumniReportRequest request = createCreateRequest(ReportScope.PERIOD,
                ReportMetric.REFERRAL_CONVERSION_RATE, null);
        when(alumniReferralRepository.count()).thenReturn(0L);
        AlumniReport savedReport = createAlumniReport(100L, ReportScope.PERIOD, ReportMetric.REFERRAL_CONVERSION_RATE,
                "0.00% (0 hired / 0 referrals)");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.PERIOD,
                ReportMetric.REFERRAL_CONVERSION_RATE, "0.00% (0 hired / 0 referrals)");

        when(reportRepository.save(any(AlumniReport.class))).thenReturn(savedReport);
        when(mapper.toResponse(savedReport)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.createReport(request);

        assertNotNull(response);
        ArgumentCaptor<AlumniReport> reportCaptor = ArgumentCaptor.forClass(AlumniReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals("0.00% (0 hired / 0 referrals)", reportCaptor.getValue().getMetricValue());
    }

    // --- getReportById Tests ---

    @Test
    void getReportById_Success() {
        AlumniReport report = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "Value");
        AlumniReportResponse expectedResponse = createAlumniReportResponse(100L, ReportScope.GRADE,
                ReportMetric.ATTRITION_RATE, "Value");

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(mapper.toResponse(report)).thenReturn(expectedResponse);

        AlumniReportResponse response = service.getReportById(100L);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(reportRepository).findById(100L);
        verify(mapper).toResponse(report);
    }

    @Test
    void getReportById_ThrowsException_WhenNotFound() {
        when(reportRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getReportById(100L);
        });

        assertEquals("Alumni report not found with ID: 100", exception.getMessage());
        verify(reportRepository).findById(100L);
        verifyNoInteractions(mapper);
    }

    // --- getAllReports Tests ---

    @Test
    void getAllReports_Success() {
        AlumniReport report = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "Value");
        AlumniReportResponse response = createAlumniReportResponse(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE,
                "Value");

        when(reportRepository.findAll()).thenReturn(Collections.singletonList(report));
        when(mapper.toResponse(report)).thenReturn(response);

        List<AlumniReportResponse> responses = service.getAllReports();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(reportRepository).findAll();
        verify(mapper).toResponse(report);
    }

    // --- getReportsByScope Tests ---

    @Test
    void getReportsByScope_Success() {
        AlumniReport report = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "Value");
        AlumniReportResponse response = createAlumniReportResponse(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE,
                "Value");

        when(reportRepository.findByScope(ReportScope.GRADE)).thenReturn(Collections.singletonList(report));
        when(mapper.toResponse(report)).thenReturn(response);

        List<AlumniReportResponse> responses = service.getReportsByScope(ReportScope.GRADE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(reportRepository).findByScope(ReportScope.GRADE);
        verify(mapper).toResponse(report);
    }

    // --- getReportsByMetric Tests ---

    @Test
    void getReportsByMetric_Success() {
        AlumniReport report = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "Value");
        AlumniReportResponse response = createAlumniReportResponse(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE,
                "Value");

        when(reportRepository.findByMetric(ReportMetric.ATTRITION_RATE)).thenReturn(Collections.singletonList(report));
        when(mapper.toResponse(report)).thenReturn(response);

        List<AlumniReportResponse> responses = service.getReportsByMetric(ReportMetric.ATTRITION_RATE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
        verify(reportRepository).findByMetric(ReportMetric.ATTRITION_RATE);
        verify(mapper).toResponse(report);
    }

    // --- deleteReport Tests ---

    @Test
    void deleteReport_Success() {
        AlumniReport report = createAlumniReport(100L, ReportScope.GRADE, ReportMetric.ATTRITION_RATE, "Value");

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));
        doNothing().when(reportRepository).delete(report);

        assertDoesNotThrow(() -> {
            service.deleteReport(100L);
        });

        verify(reportRepository).findById(100L);
        verify(reportRepository).delete(report);
    }

    @Test
    void deleteReport_ThrowsException_WhenNotFound() {
        when(reportRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteReport(100L);
        });

        assertEquals("Alumni report not found with ID: 100", exception.getMessage());
        verify(reportRepository).findById(100L);
        verifyNoMoreInteractions(reportRepository);
    }
}
