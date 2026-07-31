package com.aluminibackend.api.module.exitsurvey.controller;

import com.aluminibackend.api.module.exitsurvey.dto.request.SubmitExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.request.UpdateExitSurveyRequest;
import com.aluminibackend.api.module.exitsurvey.dto.response.ExitSurveyResponseDto;
import com.aluminibackend.api.module.exitsurvey.service.ExitSurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exit-surveys")
@RequiredArgsConstructor
public class ExitSurveyController {

    private final ExitSurveyService surveyService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP', 'EMPLOYEE')")
    @PostMapping
    public ResponseEntity<ExitSurveyResponseDto> submitSurvey(
            @Valid @RequestBody SubmitExitSurveyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(surveyService.submitSurvey(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<List<ExitSurveyResponseDto>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/{responseId}")
    public ResponseEntity<ExitSurveyResponseDto> getSurveyById(
            @PathVariable Long responseId
    ) {
        return ResponseEntity.ok(surveyService.getSurveyById(responseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ExitSurveyResponseDto> getSurveyByCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(surveyService.getSurveyByCase(caseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ExitSurveyResponseDto>> getSurveysByEmployee(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(surveyService.getSurveysByEmployee(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/anonymous")
    public ResponseEntity<List<ExitSurveyResponseDto>> getAnonymousSurveys() {
        return ResponseEntity.ok(surveyService.getAnonymousSurveys());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PutMapping("/{responseId}")
    public ResponseEntity<ExitSurveyResponseDto> updateSurvey(
            @PathVariable Long responseId,
            @Valid @RequestBody UpdateExitSurveyRequest request
    ) {
        return ResponseEntity.ok(surveyService.updateSurvey(responseId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{responseId}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable Long responseId
    ) {
        surveyService.deleteSurvey(responseId);
        return ResponseEntity.noContent().build();
    }
}
