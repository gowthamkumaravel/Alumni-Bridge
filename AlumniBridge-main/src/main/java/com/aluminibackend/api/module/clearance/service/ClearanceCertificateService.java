package com.aluminibackend.api.module.clearance.service;

import com.aluminibackend.api.module.clearance.dto.request.CreateClearanceCertificateRequest;
import com.aluminibackend.api.module.clearance.dto.response.ClearanceCertificateResponse;
import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;

import java.util.List;

public interface ClearanceCertificateService {

    ClearanceCertificateResponse issueCertificate(
            CreateClearanceCertificateRequest request
    );

    ClearanceCertificateResponse getCertificateById(Long certId);

    ClearanceCertificateResponse getCertificateByCase(Long caseId);

    List<ClearanceCertificateResponse> getAllCertificates();

    List<ClearanceCertificateResponse> getCertificatesByStatus(
            ClearanceStatus status
    );

    void deleteCertificate(Long certId);
}