package com.aluminibackend.api.module.audit.service;

import com.aluminibackend.api.integration.audit.AuditFeignClient;
import com.aluminibackend.api.integration.audit.RecordAuditRequest;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Feign adapter for the audit write path. Contains no persistence logic: it
 * resolves the acting user's name snapshot and delegates to audit-service over
 * Eureka-discovered Feign.
 *
 * <p>record(...) is best-effort — a downstream outage must never roll back the
 * business transaction that produced the event (audit is a side effect).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditFeignClient auditFeignClient;
    private final UserRepository userRepository;

    @Override
    public void record(String employeeId, String action, String module) {
        String userName = employeeId != null
                ? userRepository.findByEmployeeId(employeeId).map(User::getName).orElse(null)
                : null;
        try {
            auditFeignClient.record(new RecordAuditRequest(employeeId, userName, action, module));
        } catch (Exception ex) {
            log.error("Failed to record audit event action={} module={}: {}",
                    action, module, ex.getMessage());
        }
    }
}
