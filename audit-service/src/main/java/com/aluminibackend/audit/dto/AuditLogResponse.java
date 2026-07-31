package com.aluminibackend.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {

    private Long auditId;
    private String employeeId;
    private String userName;
    private String action;
    private String module;
    private LocalDateTime timestamp;
}
