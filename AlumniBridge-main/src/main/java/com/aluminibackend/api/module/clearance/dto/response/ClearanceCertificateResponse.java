package com.aluminibackend.api.module.clearance.dto.response;

import com.aluminibackend.api.module.clearance.enums.ClearanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ClearanceCertificateResponse {

    private Long certId;

    private Long caseId;

    private Long employeeId;

    private String employeeName;

    private LocalDate issuedDate;

    private Long issuedById;

    private String issuedByName;

    private String outstandingItems;

    private ClearanceStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}