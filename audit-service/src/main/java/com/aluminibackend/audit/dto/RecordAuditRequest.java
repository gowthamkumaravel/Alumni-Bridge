package com.aluminibackend.audit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Internal write payload. employeeId/userName are nullable to preserve the original
 * record(...) semantics where system events may have no acting user.
 */
@Data
public class RecordAuditRequest {

    private String employeeId;

    private String userName;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Module is required")
    private String module;
}
