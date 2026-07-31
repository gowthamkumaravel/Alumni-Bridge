package com.aluminibackend.api.module.audit.service;

/**
 * Monolith-side audit contract.
 *
 * <p>After the microservice extraction this exposes only record(...), the single
 * operation the monolith's modules invoke. Audit reads (/api/audit-logs/**) are
 * served by audit-service directly via the API gateway. The single bean
 * implementing this interface is a Feign adapter (no local persistence).
 */
public interface AuditLogService {

    void record(String employeeId, String action, String module);
}
