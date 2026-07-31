package com.aluminibackend.api.module.offboarding.service;

import com.aluminibackend.api.module.offboarding.dto.request.CreateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.request.UpdateOffboardingCaseRequest;
import com.aluminibackend.api.module.offboarding.dto.response.OffboardingCaseResponse;
import com.aluminibackend.api.module.offboarding.enums.OffboardingStatus;

import java.util.List;

public interface OffboardingCaseService {

    OffboardingCaseResponse createCase(CreateOffboardingCaseRequest request);

    OffboardingCaseResponse getCaseById(Long caseId);

    List<OffboardingCaseResponse> getAllCases();

    List<OffboardingCaseResponse> getCasesByEmployee(Long employeeId);

    List<OffboardingCaseResponse> getCasesByHrbpOwner(Long hrbpOwnerId);

    List<OffboardingCaseResponse> getCasesByStatus(OffboardingStatus status);

    OffboardingCaseResponse updateCase(Long caseId, UpdateOffboardingCaseRequest request);

    OffboardingCaseResponse updateStatus(Long caseId, OffboardingStatus status);

    void deleteCase(Long caseId);
}