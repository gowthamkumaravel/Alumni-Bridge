package com.aluminibackend.api.integration.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent to audit-service's internal write endpoint. Carries the acting
 * user's id and a name snapshot resolved by the monolith.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordAuditRequest {

    private String employeeId;
    private String userName;
    private String action;
    private String module;
}
