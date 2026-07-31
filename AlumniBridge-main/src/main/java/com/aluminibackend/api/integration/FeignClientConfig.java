package com.aluminibackend.api.integration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Shared Feign configuration for the notification-service and audit-service clients.
 *
 * <p>Two headers are attached on every outbound call:
 * <ul>
 *   <li><b>X-Internal-Token</b> — always sent so /internal/** write endpoints accept the call.</li>
 *   <li><b>Authorization</b> — the end-user JWT is propagated when a request context exists
 *       (i.e. public read proxies), so downstream @PreAuthorize checks see the real user.</li>
 * </ul>
 */
@Configuration
public class FeignClientConfig {

    @Value("${internal.api.token}")
    private String internalToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Internal-Token", internalToken);

            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                String authorization = sra.getRequest().getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
