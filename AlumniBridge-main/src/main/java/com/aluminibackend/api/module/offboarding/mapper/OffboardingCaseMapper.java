package com.aluminibackend.api.module.offboarding.mapper;

import com.aluminibackend.api.module.offboarding.dto.response.OffboardingCaseResponse;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import org.springframework.stereotype.Component;

@Component
public class OffboardingCaseMapper {

    public OffboardingCaseResponse toResponse(OffboardingCase offboardingCase) {
        return OffboardingCaseResponse.builder()
                .caseId(offboardingCase.getCaseId())
                .employeeId(offboardingCase.getEmployee().getUserId())
                .employeeName(offboardingCase.getEmployee().getName())
                .employeeEmail(offboardingCase.getEmployee().getEmail())
                .departureType(offboardingCase.getDepartureType())
                .lastWorkingDate(offboardingCase.getLastWorkingDate())
                .hrbpOwnerId(offboardingCase.getHrbpOwner().getUserId())
                .hrbpOwnerName(offboardingCase.getHrbpOwner().getName())
                .status(offboardingCase.getStatus())
                .createdAt(offboardingCase.getCreatedAt())
                .updatedAt(offboardingCase.getUpdatedAt())
                .build();
    }
}