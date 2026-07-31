package com.aluminibackend.audit.service;

import com.aluminibackend.audit.dto.AuditLogResponse;
import com.aluminibackend.audit.dto.RecordAuditRequest;
import com.aluminibackend.audit.entity.AuditLog;
import com.aluminibackend.audit.mapper.AuditLogMapper;
import com.aluminibackend.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic preserved from the monolith's AuditLogServiceImpl. The only
 * change is that the User is no longer loaded from a repository; the acting
 * user's id/name arrive as a snapshot in the request.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper mapper;

    @Override
    public void record(RecordAuditRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .employeeId(request.getEmployeeId())
                .userName(request.getUserName())
                .action(request.getAction())
                .module(request.getModule())
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByUser(String employeeId) {
        return auditLogRepository.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByModule(String module) {
        return auditLogRepository.findByModule(module)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
