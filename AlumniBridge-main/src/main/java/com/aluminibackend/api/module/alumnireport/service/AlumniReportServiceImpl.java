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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniReportServiceImpl implements AlumniReportService {

    private final AlumniReportRepository reportRepository;
    private final AlumniReportMapper mapper;
    private final UserRepository userRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final ExitSurveyResponseRepository exitSurveyRepository;
    private final ExitInterviewRepository exitInterviewRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final BoomerangApplicationRepository boomerangApplicationRepository;
    private final AlumniReferralRepository alumniReferralRepository;

    @Override
    public AlumniReportResponse createReport(CreateAlumniReportRequest request) {

        AlumniReport report = AlumniReport.builder()
                .scope(request.getScope())
                .metric(request.getMetric())
                .metricValue(resolveMetricValue(request))
                .build();

        return mapper.toResponse(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniReportResponse getReportById(Long reportId) {
        return mapper.toResponse(findReportById(reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReportResponse> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReportResponse> getReportsByScope(ReportScope scope) {
        return reportRepository.findByScope(scope)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniReportResponse> getReportsByMetric(ReportMetric metric) {
        return reportRepository.findByMetric(metric)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void deleteReport(Long reportId) {
        AlumniReport report = findReportById(reportId);
        reportRepository.delete(report);
    }

    private AlumniReport findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(
                        "Alumni report not found with ID: " + reportId
                ));
    }

    private String resolveMetricValue(CreateAlumniReportRequest request) {
        if (request.getMetricValue() != null && !request.getMetricValue().isBlank()) {
            return request.getMetricValue();
        }

        return switch (request.getMetric()) {
            case ATTRITION_RATE -> attritionRate();
            case AVG_TENURE -> averagePreviousTenure();
            case EXIT_SURVEY_SCORE -> averageExitSurveyScore();
            case TOP_EXIT_REASONS -> topExitReasons();
            case ALUMNI_ENGAGEMENT_RATE -> alumniEngagementRate();
            case BOOMERANG_HIRE_RATE -> boomerangHireRate();
            case REFERRAL_CONVERSION_RATE -> referralConversionRate();
        };
    }

    private String attritionRate() {
        long totalUsers = userRepository.count();
        long offboardingCases = offboardingCaseRepository.count();

        if (totalUsers == 0) {
            return "0.00% (0 offboarding cases / 0 users)";
        }

        return percentage(offboardingCases, totalUsers)
                + " (" + offboardingCases + " offboarding cases / " + totalUsers + " users)";
    }

    private String averagePreviousTenure() {
        List<Double> tenureYears = boomerangApplicationRepository.findAll()
                .stream()
                .map(BoomerangApplication::getPreviousTenureYears)
                .filter(Objects::nonNull)
                .toList();

        if (tenureYears.isEmpty()) {
            return "Not available: previous tenure years have not been captured";
        }

        double average = tenureYears.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        return String.format(Locale.US, "%.2f years", average);
    }

    private String averageExitSurveyScore() {
        List<Integer> scores = exitSurveyRepository.findAll()
                .stream()
                .map(ExitSurveyResponse::getOverallScore)
                .filter(Objects::nonNull)
                .toList();

        if (scores.isEmpty()) {
            return "Not available: no exit survey scores submitted";
        }

        double average = scores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return String.format(Locale.US, "%.2f / 5", average);
    }

    private String topExitReasons() {
        Map<String, Long> reasonCounts = exitInterviewRepository.findAll()
                .stream()
                .map(ExitInterview::getPrimaryReasonForLeaving)
                .filter(reason -> reason != null && !reason.isBlank())
                .collect(Collectors.groupingBy(reason -> reason, Collectors.counting()));

        if (reasonCounts.isEmpty()) {
            return "Not available: no exit interview reasons captured";
        }

        return reasonCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private String alumniEngagementRate() {
        List<AlumniProfile> alumniProfiles = alumniProfileRepository.findAll();
        long totalAlumni = alumniProfiles.size();

        if (totalAlumni == 0) {
            return "0.00% (0 engaged alumni / 0 alumni profiles)";
        }

        long engagedAlumni = alumniProfiles.stream()
                .filter(profile -> profile.getEngagementTier() == EngagementTier.ENGAGED
                        || Boolean.TRUE.equals(profile.getCommunityOptIn()))
                .count();

        return percentage(engagedAlumni, totalAlumni)
                + " (" + engagedAlumni + " engaged alumni / " + totalAlumni + " alumni profiles)";
    }

    private String boomerangHireRate() {
        long totalApplications = boomerangApplicationRepository.count();
        long joinedApplications = boomerangApplicationRepository
                .findByStatus(BoomerangApplicationStatus.JOINED)
                .size();

        if (totalApplications == 0) {
            return "0.00% (0 joined / 0 boomerang applications)";
        }

        return percentage(joinedApplications, totalApplications)
                + " (" + joinedApplications + " joined / " + totalApplications + " boomerang applications)";
    }

    private String referralConversionRate() {
        long totalReferrals = alumniReferralRepository.count();
        long hiredReferrals = alumniReferralRepository
                .findByStatus(AlumniReferralStatus.HIRED)
                .size();

        if (totalReferrals == 0) {
            return "0.00% (0 hired / 0 referrals)";
        }

        return percentage(hiredReferrals, totalReferrals)
                + " (" + hiredReferrals + " hired / " + totalReferrals + " referrals)";
    }

    private String percentage(long numerator, long denominator) {
        double value = denominator == 0 ? 0 : (numerator * 100.0) / denominator;
        return String.format(Locale.US, "%.2f%%", value);
    }
}
