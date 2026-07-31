package com.aluminibackend.api.module.exitinterview.controller;

import com.aluminibackend.api.module.exitinterview.dto.request.CompleteExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.request.CreateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.request.UpdateExitInterviewRequest;
import com.aluminibackend.api.module.exitinterview.dto.response.ExitInterviewResponse;
import com.aluminibackend.api.module.exitinterview.enums.ExitInterviewStatus;
import com.aluminibackend.api.module.exitinterview.service.ExitInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exit-interviews")
@RequiredArgsConstructor
public class ExitInterviewController {

    private final ExitInterviewService interviewService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PostMapping
    public ResponseEntity<ExitInterviewResponse> scheduleInterview(
            @Valid @RequestBody CreateExitInterviewRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(interviewService.scheduleInterview(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<List<ExitInterviewResponse>> getAllInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/{interviewId}")
    public ResponseEntity<ExitInterviewResponse> getInterviewById(
            @PathVariable Long interviewId
    ) {
        return ResponseEntity.ok(interviewService.getInterviewById(interviewId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ExitInterviewResponse> getInterviewByCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(interviewService.getInterviewByCase(caseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ExitInterviewResponse>> getInterviewsByEmployee(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(interviewService.getInterviewsByEmployee(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<List<ExitInterviewResponse>> getInterviewsByInterviewer(
            @PathVariable String interviewerId
    ) {
        return ResponseEntity.ok(interviewService.getInterviewsByInterviewer(interviewerId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ExitInterviewResponse>> getInterviewsByStatus(
            @PathVariable ExitInterviewStatus status
    ) {
        return ResponseEntity.ok(interviewService.getInterviewsByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PutMapping("/{interviewId}")
    public ResponseEntity<ExitInterviewResponse> updateInterview(
            @PathVariable Long interviewId,
            @Valid @RequestBody UpdateExitInterviewRequest request
    ) {
        return ResponseEntity.ok(interviewService.updateInterview(interviewId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PatchMapping("/{interviewId}/complete")
    public ResponseEntity<ExitInterviewResponse> completeInterview(
            @PathVariable Long interviewId,
            @Valid @RequestBody CompleteExitInterviewRequest request
    ) {
        return ResponseEntity.ok(
                interviewService.completeInterview(
                        interviewId,
                        request.getPrimaryReasonForLeaving(),
                        request.getKeyFeedback()
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> deleteInterview(
            @PathVariable Long interviewId
    ) {
        interviewService.deleteInterview(interviewId);
        return ResponseEntity.noContent().build();
    }
}