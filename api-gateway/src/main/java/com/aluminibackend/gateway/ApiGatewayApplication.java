package com.aluminibackend.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Edge gateway. Routes public traffic to the registered services and forwards the
 * JWT Authorization header unchanged; each downstream app validates the token and
 * enforces its own @PreAuthorize rules (edge routing, downstream authorization).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
