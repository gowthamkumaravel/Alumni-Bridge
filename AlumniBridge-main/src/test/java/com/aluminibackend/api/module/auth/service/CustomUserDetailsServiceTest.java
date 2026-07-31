package com.aluminibackend.api.module.auth.service;

import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    // --- Helper Methods ---

    private User createUser(Long userId, String email, UserStatus status, UserRole role) {
        return User.builder()
                .userId(userId)
                .name("Test User")
                .email(email)
                .password("encodedPassword")
                .status(status)
                .role(role)
                .build();
    }

    // --- loadUserByUsername Tests ---

    @Test
    void loadUserByUsername_Success_ActiveUser() {
        User user = createUser(100L, "test@example.com", UserStatus.ACTIVE, UserRole.ADMIN);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_Success_InactiveUser() {
        User user = createUser(100L, "test@example.com", UserStatus.INACTIVE, UserRole.EMPLOYEE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertFalse(userDetails.isEnabled()); // disabled because status is INACTIVE
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_Success_PendingUser() {
        User user = createUser(100L, "test@example.com", UserStatus.ACTIVE, UserRole.HRBP);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertTrue(userDetails.isEnabled()); // enabled because status is not INACTIVE
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HRBP")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ThrowsUsernameNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("test@example.com");
        });

        assertEquals("User not found with email: test@example.com", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
    }
}
