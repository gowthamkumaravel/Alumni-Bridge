package com.aluminibackend.audit.mapper;

import com.aluminibackend.audit.dto.AuditLogResponse;
import com.aluminibackend.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .auditId(auditLog.getAuditId())
                .employeeId(auditLog.getEmployeeId())
                .userName(auditLog.getUserName())
                .action(auditLog.getAction())
                .module(auditLog.getModule())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
