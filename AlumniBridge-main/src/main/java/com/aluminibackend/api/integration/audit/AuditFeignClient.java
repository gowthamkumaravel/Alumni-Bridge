package com.aluminibackend.api.integration.audit;

import com.aluminibackend.api.integration.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Discovery-based Feign client for audit-service.
 *
 * <p>No {@code url} — resolved from Eureka by service name ("audit-service").
 *
 * <p>Only the internal write endpoint is used from the monolith. Public audit
 * reads (/api/audit-logs/**) are routed by the API gateway directly to
 * audit-service, so no read methods are needed here.
 */
@FeignClient(
        name = "audit-service",
        configuration = FeignClientConfig.class
)
public interface AuditFeignClient {

    @PostMapping("/internal/audit-logs")
    void record(@RequestBody RecordAuditRequest request);
}
