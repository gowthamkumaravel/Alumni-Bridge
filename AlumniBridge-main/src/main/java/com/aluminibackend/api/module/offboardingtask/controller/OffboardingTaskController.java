package com.aluminibackend.api.module.offboardingtask.controller;

import com.aluminibackend.api.module.offboardingtask.dto.request.CreateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.dto.request.UpdateOffboardingTaskRequest;
import com.aluminibackend.api.module.offboardingtask.service.OffboardingTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offboarding-tasks")
@RequiredArgsConstructor
public class OffboardingTaskController {

    private final OffboardingTaskService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HRBP')")
    public ResponseEntity<?> create(
            @RequestBody
            @Valid
            CreateOffboardingTaskRequest request
    ) {
        return ResponseEntity.ok(
                service.createTask(request)
        );
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("@securityAccessService.canAccessTask(#taskId, authentication)")
    public ResponseEntity<?> get(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                service.getTask(taskId)
        );
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'IT_OPS')")
    public ResponseEntity<?> getByCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(
                service.getTasksByCase(caseId)
        );
    }

    @GetMapping("/assignee/{userId}")
    @PreAuthorize("@securityAccessService.canAccessUser(#userId, authentication)")
    public ResponseEntity<?> getByAssignee(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                service.getTasksByAssignee(userId)
        );
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'IT_OPS')")
    public ResponseEntity<?> update(
            @PathVariable Long taskId,
            @RequestBody @Valid UpdateOffboardingTaskRequest request
    ) {
        return ResponseEntity.ok(
                service.updateTask(taskId, request)
        );
    }

    @PatchMapping("/{taskId}/complete")
    @PreAuthorize("@securityAccessService.canAccessTask(#taskId, authentication)")
    public ResponseEntity<?> complete(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                service.markCompleted(taskId)
        );
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long taskId
    ) {

        service.deleteTask(taskId);

        return ResponseEntity.noContent()
                .build();
    }
}
