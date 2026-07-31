package com.aluminibackend.api.module.exitinterview.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.exitinterview.dto.request.CreateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.request.UpdateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.response.ExitInterviewResponse;
import com.aluminibackend.api.module.exitinterview.entity.ExitInterview;
import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import com.aluminibackend.api.module.exitinterview.mapper.ExitInterviewMapper;
import com.aluminibackend.api.module.exitinterview.repository.ExitInterviewRepository;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExitInterviewServiceImpl implements ExitInterviewService {

    private final ExitInterviewRepository interviewRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final UserRepository userRepository;
    private final ExitInterviewMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public ExitInterviewResponse scheduleInterview(CreateExitInterviewRequest request) {

        if (interviewRepository.existsByOffboardingCase_CaseId(request.getCaseId())) {
            throw new RuntimeException("Exit interview already scheduled for this case");
        }

        OffboardingCase offboardingCase = offboardingCaseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Offboarding case not found"));

        User interviewer = userRepository.findByEmployeeId(request.getInterviewerId())
                .orElseThrow(() -> new RuntimeException("Interviewer not found"));

        if (interviewer.getRole() != UserRole.HRBP && interviewer.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only HRBP or ADMIN can conduct exit interview");
        }

        ExitInterview interview = ExitInterview.builder()
                .offboardingCase(offboardingCase)
                .employee(offboardingCase.getEmployee())
                .interviewer(interviewer)
                .scheduledDate(request.getScheduledDate())
                .format(request.getFormat())
                .status(ExitInterviewStatus.SCHEDULED)
                .build();

        ExitInterview savedInterview = interviewRepository.save(interview);
        notificationService.createSystemNotification(
                offboardingCase.getEmployee().getEmployeeId(),
                "Your exit interview has been scheduled.",
                NotificationCategory.EXIT_INTERVIEW
        );
        auditLogService.record(interviewer.getEmployeeId(), "SCHEDULE_EXIT_INTERVIEW", "EXIT_INTERVIEW");

        return mapper.toResponse(savedInterview);
    }

    @Override
    @Transactional(readOnly = true)
    public ExitInterviewResponse getInterviewById(Long interviewId) {
        return mapper.toResponse(findInterviewById(interviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public ExitInterviewResponse getInterviewByCase(Long caseId) {
        ExitInterview interview = interviewRepository.findByOffboardingCase_CaseId(caseId)
                .orElseThrow(() -> new RuntimeException("Exit interview not found for case ID: " + caseId));

        return mapper.toResponse(interview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitInterviewResponse> getAllInterviews() {
        return interviewRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitInterviewResponse> getInterviewsByEmployee(String employeeId) {
        return interviewRepository.findByEmployee_EmployeeId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitInterviewResponse> getInterviewsByInterviewer(String interviewerId) {
        return interviewRepository.findByInterviewer_EmployeeId(interviewerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExitInterviewResponse> getInterviewsByStatus(ExitInterviewStatus status) {
        return interviewRepository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ExitInterviewResponse updateInterview(
            Long interviewId,
            UpdateExitInterviewRequest request
    ) {
        ExitInterview interview = findInterviewById(interviewId);

        if (request.getInterviewerId() != null) {
            User interviewer = userRepository.findByEmployeeId(request.getInterviewerId())
                    .orElseThrow(() -> new RuntimeException("Interviewer not found"));

            if (interviewer.getRole() != UserRole.HRBP && interviewer.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Only HRBP or ADMIN can conduct exit interview");
            }

            interview.setInterviewer(interviewer);
        }

        if (request.getScheduledDate() != null) {
            interview.setScheduledDate(request.getScheduledDate());
        }

        if (request.getFormat() != null) {
            interview.setFormat(request.getFormat());
        }

        if (request.getPrimaryReasonForLeaving() != null) {
            interview.setPrimaryReasonForLeaving(request.getPrimaryReasonForLeaving());
        }

        if (request.getKeyFeedback() != null) {
            interview.setKeyFeedback(request.getKeyFeedback());
        }

        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
        }

        ExitInterview updatedInterview = interviewRepository.save(interview);
        auditLogService.record(
                updatedInterview.getInterviewer().getEmployeeId(),
                "UPDATE_EXIT_INTERVIEW",
                "EXIT_INTERVIEW"
        );

        return mapper.toResponse(updatedInterview);
    }

    @Override
    public ExitInterviewResponse completeInterview(
            Long interviewId,
            String primaryReason,
            String feedback
    ) {
        ExitInterview interview = findInterviewById(interviewId);

        interview.setPrimaryReasonForLeaving(primaryReason);
        interview.setKeyFeedback(feedback);
        interview.setStatus(ExitInterviewStatus.COMPLETED);

        ExitInterview updatedInterview = interviewRepository.save(interview);
        notificationService.createSystemNotification(
                interview.getEmployee().getEmployeeId(),
                "Your exit interview has been marked completed.",
                NotificationCategory.EXIT_INTERVIEW
        );
        auditLogService.record(
                interview.getInterviewer().getEmployeeId(),
                "COMPLETE_EXIT_INTERVIEW",
                "EXIT_INTERVIEW"
        );

        return mapper.toResponse(updatedInterview);
    }

    @Override
    public void deleteInterview(Long interviewId) {
        ExitInterview interview = findInterviewById(interviewId);
        interviewRepository.delete(interview);
    }

    private ExitInterview findInterviewById(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException(
                        "Exit interview not found with ID: " + interviewId
                ));
    }
}
