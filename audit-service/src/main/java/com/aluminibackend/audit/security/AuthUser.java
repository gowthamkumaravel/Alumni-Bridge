package com.aluminibackend.audit.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal reconstructed purely from JWT claims (no DB lookup).
 * getUsername() returns the email so ownership-by-email checks keep working.
 */
public class AuthUser implements UserDetails {

    private final String employeeId;
    private final String email;
    private final String role;

    public AuthUser(String employeeId, String email, String role) {
        this.employeeId = employeeId;
        this.email = email;
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
