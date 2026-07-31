package com.aluminibackend.api.module.knowledgehandover.controller;

import com.aluminibackend.api.module.knowledgehandover.dto.request.CreateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.request.UpdateKnowledgeHandoverRequest;
import com.aluminibackend.api.module.knowledgehandover.dto.response.KnowledgeHandoverResponse;
import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import com.aluminibackend.api.module.knowledgehandover.service.KnowledgeHandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-handovers")
@RequiredArgsConstructor
public class KnowledgeHandoverController {

    private final KnowledgeHandoverService handoverService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PostMapping
    public ResponseEntity<KnowledgeHandoverResponse> createHandover(
            @Valid @RequestBody CreateKnowledgeHandoverRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(handoverService.createHandover(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<List<KnowledgeHandoverResponse>> getAllHandovers() {
        return ResponseEntity.ok(handoverService.getAllHandovers());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/{handoverId}")
    public ResponseEntity<KnowledgeHandoverResponse> getHandoverById(
            @PathVariable Long handoverId
    ) {
        return ResponseEntity.ok(handoverService.getHandoverById(handoverId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<KnowledgeHandoverResponse> getHandoverByCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(handoverService.getHandoverByCase(caseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<KnowledgeHandoverResponse>> getHandoversByEmployee(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(handoverService.getHandoversByEmployee(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/successor/{successorId}")
    public ResponseEntity<List<KnowledgeHandoverResponse>> getHandoversBySuccessor(
            @PathVariable Long successorId
    ) {
        return ResponseEntity.ok(handoverService.getHandoversBySuccessor(successorId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KnowledgeHandoverResponse>> getHandoversByStatus(
            @PathVariable HandoverStatus status
    ) {
        return ResponseEntity.ok(handoverService.getHandoversByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PutMapping("/{handoverId}")
    public ResponseEntity<KnowledgeHandoverResponse> updateHandover(
            @PathVariable Long handoverId,
            @Valid @RequestBody UpdateKnowledgeHandoverRequest request
    ) {
        return ResponseEntity.ok(handoverService.updateHandover(handoverId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PatchMapping("/{handoverId}/complete")
    public ResponseEntity<KnowledgeHandoverResponse> markCompleted(
            @PathVariable Long handoverId
    ) {
        return ResponseEntity.ok(handoverService.markCompleted(handoverId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PatchMapping("/{handoverId}/manager-signoff")
    public ResponseEntity<KnowledgeHandoverResponse> managerSignOff(
            @PathVariable Long handoverId
    ) {
        return ResponseEntity.ok(handoverService.managerSignOff(handoverId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{handoverId}")
    public ResponseEntity<Void> deleteHandover(
            @PathVariable Long handoverId
    ) {
        handoverService.deleteHandover(handoverId);
        return ResponseEntity.noContent().build();
    }
}