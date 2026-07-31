package com.aluminibackend.api.module.clearance.mapper;

import com.aluminibackend.api.module.clearance.dto.response.ClearanceCertificateResponse;
import com.aluminibackend.api.module.clearance.entity.ClearanceCertificate;
import org.springframework.stereotype.Component;

@Component
public class ClearanceCertificateMapper {

    public ClearanceCertificateResponse toResponse(
            ClearanceCertificate certificate
    ) {
        return ClearanceCertificateResponse.builder()
                .certId(certificate.getCertId())
                .caseId(certificate.getOffboardingCase().getCaseId())
                .employeeId(certificate.getOffboardingCase().getEmployee().getUserId())
                .employeeName(certificate.getOffboardingCase().getEmployee().getName())
                .issuedDate(certificate.getIssuedDate())
                .issuedById(certificate.getIssuedBy().getUserId())
                .issuedByName(certificate.getIssuedBy().getName())
                .outstandingItems(certificate.getOutstandingItems())
                .status(certificate.getStatus())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
}