package com.aluminibackend.audit.service;

import com.aluminibackend.audit.dto.AuditLogResponse;
import com.aluminibackend.audit.dto.RecordAuditRequest;

import java.util.List;

public interface AuditLogService {

    void record(RecordAuditRequest request);

    List<AuditLogResponse> getAllLogs();

    List<AuditLogResponse> getLogsByUser(String employeeId);

    List<AuditLogResponse> getLogsByModule(String module);
}
