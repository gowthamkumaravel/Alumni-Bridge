package com.aluminibackend.api.module.offboardingtask.service;

import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.offboarding.repository.OffboardingCaseRepository;
import com.aluminibackend.api.module.offboardingtask.dto.request.CreateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.dto.request.UpdateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.dto.response.OffboardingTaskResponse;
import com.aluminibackend.api.module.offboardingtask.entity.OffboardingTask;
import com.aluminibackend.api.module.offboardingtask.enums.*;
import com.aluminibackend.api.module.offboardingtask.mapper.OffboardingTaskMapper;
import com.aluminibackend.api.module.offboardingtask.repository.OffboardingTaskRepository;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OffboardingTaskServiceImpl implements OffboardingTaskService {

    private final OffboardingTaskRepository taskRepository;
    private final OffboardingCaseRepository offboardingCaseRepository;
    private final UserRepository userRepository;
    private final OffboardingTaskMapper mapper;
    private final NotificationService notificationService;

    @Override
    public OffboardingTaskResponse createTask(
            CreateOffboardingTaskRequest request
    ) {

        OffboardingCase offboardingCase =
                offboardingCaseRepository.findById(
                        request.getCaseId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Offboarding Case not found"
                        ));

        User assignedUser =
                userRepository.findById(
                        request.getAssignedToId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Assigned User not found"
                        ));

        OffboardingTask task =
                OffboardingTask.builder()
                        .offboardingCase(offboardingCase)
                        .category(request.getCategory())
                        .taskDescription(
                                request.getTaskDescription()
                        )
                        .assignedTo(assignedUser)
                        .dueDate(request.getDueDate())
                        .status(TaskStatus.PENDING)
                        .build();

        OffboardingTask savedTask =
                taskRepository.save(task);

        notificationService.createSystemNotification(
                assignedUser.getEmployeeId(),
                "New offboarding task assigned: " + savedTask.getTaskDescription(),
                NotificationCategory.OFFBOARDING
        );

        return mapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public OffboardingTaskResponse getTask(
            Long taskId
    ) {

        OffboardingTask task =
                findTaskById(taskId);

        return mapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffboardingTaskResponse> getTasksByCase(
            Long caseId
    ) {

        return taskRepository
                .findByOffboardingCase_CaseId(caseId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffboardingTaskResponse> getTasksByAssignee(
            Long userId
    ) {

        return taskRepository
                .findByAssignedTo_UserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public OffboardingTaskResponse updateTask(
            Long taskId,
            UpdateOffboardingTaskRequest request
    ) {

        OffboardingTask task =
                findTaskById(taskId);

        if (request.getTaskDescription() != null) {
            task.setTaskDescription(
                    request.getTaskDescription()
            );
        }

        if (request.getAssignedToId() != null) {

            User assignedUser =
                    userRepository.findById(
                            request.getAssignedToId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Assigned User not found"
                            ));

            task.setAssignedTo(assignedUser);
        }

        if (request.getDueDate() != null) {
            task.setDueDate(
                    request.getDueDate()
            );
        }

        if (request.getStatus() != null) {

            task.setStatus(
                    request.getStatus()
            );

            if (request.getStatus()
                    == TaskStatus.COMPLETED) {

                task.setCompletedDate(
                        LocalDate.now()
                );
            }
        }

        OffboardingTask updatedTask =
                taskRepository.save(task);

        return mapper.toResponse(updatedTask);
    }

    @Override
    public OffboardingTaskResponse markCompleted(
            Long taskId
    ) {

        OffboardingTask task =
                findTaskById(taskId);

        task.setStatus(
                TaskStatus.COMPLETED
        );

        task.setCompletedDate(
                LocalDate.now()
        );

        OffboardingTask updatedTask =
                taskRepository.save(task);

        notificationService.createSystemNotification(
                task.getOffboardingCase().getHrbpOwner().getEmployeeId(),
                "Offboarding task completed: " + task.getTaskDescription(),
                NotificationCategory.OFFBOARDING
        );

        return mapper.toResponse(updatedTask);
    }

    @Override
    public void deleteTask(
            Long taskId
    ) {

        OffboardingTask task =
                findTaskById(taskId);

        taskRepository.delete(task);
    }

    private OffboardingTask findTaskById(
            Long taskId
    ) {

        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with id : "
                                        + taskId
                        ));
    }
}
