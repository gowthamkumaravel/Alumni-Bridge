package com.aluminibackend.audit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards /internal/** endpoints (service-to-service writes from the monolith)
 * with a shared static token. Public /api/** paths are untouched and rely on JWT.
 */
@Component
public class InternalAuthFilter extends OncePerRequestFilter {

    @Value("${internal.api.token}")
    private String internalToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().startsWith("/internal/")) {
            String provided = request.getHeader("X-Internal-Token");
            if (provided == null || !provided.equals(internalToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid internal token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
