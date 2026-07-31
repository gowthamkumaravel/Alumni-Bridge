package com.aluminibackend.api.module.handoverdocument.controller;

import com.aluminibackend.api.module.handoverdocument.dto.request.CreateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.request.UpdateHandoverDocumentRequest;
import com.aluminibackend.api.module.handoverdocument.dto.response.HandoverDocumentResponse;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import com.aluminibackend.api.module.handoverdocument.service.HandoverDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handover-documents")
@RequiredArgsConstructor
public class HandoverDocumentController {

    private final HandoverDocumentService documentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PostMapping
    public ResponseEntity<HandoverDocumentResponse> uploadDocument(
            @Valid @RequestBody CreateHandoverDocumentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/{docId}")
    public ResponseEntity<HandoverDocumentResponse> getDocumentById(
            @PathVariable Long docId
    ) {
        return ResponseEntity.ok(documentService.getDocumentById(docId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @GetMapping("/handover/{handoverId}")
    public ResponseEntity<List<HandoverDocumentResponse>> getDocumentsByHandover(
            @PathVariable Long handoverId
    ) {
        return ResponseEntity.ok(documentService.getDocumentsByHandover(handoverId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/type/{documentType}")
    public ResponseEntity<List<HandoverDocumentResponse>> getDocumentsByType(
            @PathVariable DocumentType documentType
    ) {
        return ResponseEntity.ok(documentService.getDocumentsByType(documentType));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<HandoverDocumentResponse>> getDocumentsByStatus(
            @PathVariable DocumentStatus status
    ) {
        return ResponseEntity.ok(documentService.getDocumentsByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PutMapping("/{docId}")
    public ResponseEntity<HandoverDocumentResponse> updateDocument(
            @PathVariable Long docId,
            @Valid @RequestBody UpdateHandoverDocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.updateDocument(docId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PatchMapping("/{docId}/review")
    public ResponseEntity<HandoverDocumentResponse> markReviewedBySuccessor(
            @PathVariable Long docId
    ) {
        return ResponseEntity.ok(documentService.markReviewedBySuccessor(docId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{docId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long docId
    ) {
        documentService.deleteDocument(docId);
        return ResponseEntity.noContent().build();
    }
}