package com.aluminibackend.api.module.offboarding.service;

import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.offboarding.dto.request.CreateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.request.UpdateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.response.OffboardingCaseResponse;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;
import com.aluminibackend.api.module.offboarding.mapper.OffboardingCaseMapper;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import com.aluminibackend.api.module.offboardingtask.enums.TaskCategory;
import com.aluminibackend.api.module.offboardingtask.enums.TaskStatus;
import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OffboardingCaseServiceImpl implements OffboardingCaseService {

    private final OffboardingCaseRepository offboardingCaseRepository;
    private final OffboardingTaskRepository offboardingTaskRepository;
    private final UserRepository userRepository;
    private final OffboardingCaseMapper offboardingCaseMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public OffboardingCaseResponse createCase(CreateOffboardingCaseRequest request) {

        if (offboardingCaseRepository.existsByEmployee_UserIdAndStatusNot(
                request.getEmployeeId(),
                OffboardingStatus.COMPLETED
        )) {
            throw new RuntimeException("Active offboarding case already exists for this employee");
        }

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        User hrbpOwner = userRepository.findById(request.getHrbpOwnerId())
                .orElseThrow(() -> new RuntimeException("HRBP owner not found"));

        if (hrbpOwner.getRole() != UserRole.HRBP && hrbpOwner.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("HRBP owner must have HRBP or ADMIN role");
        }

        OffboardingCase offboardingCase = OffboardingCase.builder()
                .employee(employee)
                .departureType(request.getDepartureType())
                .lastWorkingDate(request.getLastWorkingDate())
                .hrbpOwner(hrbpOwner)
                .status(OffboardingStatus.INITIATED)
                .build();

        OffboardingCase savedCase = offboardingCaseRepository.save(offboardingCase);
        generateDefaultChecklist(savedCase);

        notificationService.createSystemNotification(
                employee.getEmployeeId(),
                "Your offboarding case has been initiated. Please review your checklist.",
                NotificationCategory.OFFBOARDING
        );

        notificationService.createSystemNotification(
                hrbpOwner.getEmployeeId(),
                "Offboarding case created for " + employee.getName() + ".",
                NotificationCategory.OFFBOARDING
        );
        auditLogService.record(hrbpOwner.getEmployeeId(), "CREATE_OFFBOARDING_CASE", "OFFBOARDING");

        return offboardingCaseMapper.toResponse(savedCase);
    }

    @Override
    public OffboardingCaseResponse getCaseById(Long caseId) {
        return offboardingCaseMapper.toResponse(findCaseById(caseId));
    }

    @Override
    public List<OffboardingCaseResponse> getAllCases() {
        return offboardingCaseRepository.findAll()
                .stream()
                .map(offboardingCaseMapper::toResponse)
                .toList();
    }

    @Override
    public List<OffboardingCaseResponse> getCasesByEmployee(Long employeeId) {
        return offboardingCaseRepository.findByEmployee_UserId(employeeId)
                .stream()
                .map(offboardingCaseMapper::toResponse)
                .toList();
    }

    @Override
    public List<OffboardingCaseResponse> getCasesByHrbpOwner(Long hrbpOwnerId) {
        return offboardingCaseRepository.findByHrbpOwner_UserId(hrbpOwnerId)
                .stream()
                .map(offboardingCaseMapper::toResponse)
                .toList();
    }

    @Override
    public List<OffboardingCaseResponse> getCasesByStatus(OffboardingStatus status) {
        return offboardingCaseRepository.findByStatus(status)
                .stream()
                .map(offboardingCaseMapper::toResponse)
                .toList();
    }

    @Override
    public OffboardingCaseResponse updateCase(
            Long caseId,
            UpdateOffboardingCaseRequest request
    ) {
        OffboardingCase offboardingCase = findCaseById(caseId);

        if (request.getDepartureType() != null) {
            offboardingCase.setDepartureType(request.getDepartureType());
        }

        if (request.getLastWorkingDate() != null) {
            offboardingCase.setLastWorkingDate(request.getLastWorkingDate());
        }

        if (request.getHrbpOwnerId() != null) {
            User hrbpOwner = userRepository.findById(request.getHrbpOwnerId())
                    .orElseThrow(() -> new RuntimeException("HRBP owner not found"));

            if (hrbpOwner.getRole() != UserRole.HRBP && hrbpOwner.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("HRBP owner must have HRBP or ADMIN role");
            }

            offboardingCase.setHrbpOwner(hrbpOwner);
        }

        if (request.getStatus() != null) {
            offboardingCase.setStatus(request.getStatus());
        }

        OffboardingCase updatedCase = offboardingCaseRepository.save(offboardingCase);
        auditLogService.record(
                updatedCase.getHrbpOwner().getEmployeeId(),
                "UPDATE_OFFBOARDING_CASE",
                "OFFBOARDING"
        );

        return offboardingCaseMapper.toResponse(updatedCase);
    }

    @Override
    public OffboardingCaseResponse updateStatus(Long caseId, OffboardingStatus status) {
        OffboardingCase offboardingCase = findCaseById(caseId);
        offboardingCase.setStatus(status);

        OffboardingCase updatedCase = offboardingCaseRepository.save(offboardingCase);
        auditLogService.record(
                updatedCase.getHrbpOwner().getEmployeeId(),
                "UPDATE_OFFBOARDING_STATUS_" + status,
                "OFFBOARDING"
        );

        return offboardingCaseMapper.toResponse(updatedCase);
    }

    @Override
    public void deleteCase(Long caseId) {
        OffboardingCase offboardingCase = findCaseById(caseId);
        offboardingCaseRepository.delete(offboardingCase);
    }

    private OffboardingCase findCaseById(Long caseId) {
        return offboardingCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Offboarding case not found with ID: " + caseId));
    }

    private void generateDefaultChecklist(OffboardingCase offboardingCase) {
        User employee = offboardingCase.getEmployee();
        User hrbpOwner = offboardingCase.getHrbpOwner();
        User itOwner = userRepository.findByRole(UserRole.IT_OPS)
                .stream()
                .findFirst()
                .orElse(hrbpOwner);

        createDefaultTask(
                offboardingCase,
                TaskCategory.HR,
                "Complete exit survey",
                employee,
                5
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.HR,
                "Register or confirm alumni community preference",
                employee,
                3
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.MANAGER,
                "Submit knowledge handover notes and active project summary",
                employee,
                7
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.IT,
                "Revoke system access and collect assigned devices",
                itOwner,
                2
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.FINANCE,
                "Verify expense, advance, and payroll clearance",
                hrbpOwner,
                2
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.FACILITIES,
                "Confirm badge, workspace, and physical asset return",
                hrbpOwner,
                2
        );

        createDefaultTask(
                offboardingCase,
                TaskCategory.LEGAL,
                "Confirm policy, NDA, and compliance clearance",
                hrbpOwner,
                1
        );
    }

    private void createDefaultTask(
            OffboardingCase offboardingCase,
            TaskCategory category,
            String description,
            User assignee,
            int dueDaysBeforeLastWorkingDate
    ) {
        OffboardingTask task = OffboardingTask.builder()
                .offboardingCase(offboardingCase)
                .category(category)
                .taskDescription(description)
                .assignedTo(assignee)
                .dueDate(offboardingCase.getLastWorkingDate().minusDays(dueDaysBeforeLastWorkingDate))
                .status(TaskStatus.PENDING)
                .build();

        offboardingTaskRepository.save(task);

        notificationService.createSystemNotification(
                assignee.getEmployeeId(),
                "New offboarding task assigned: " + description,
                NotificationCategory.OFFBOARDING
        );
    }
}
