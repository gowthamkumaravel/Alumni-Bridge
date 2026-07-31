package com.aluminibackend.api.module.auth.service;

import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.auth.dto.request.LoginRequest;
import com.aluminibackend.api.module.auth.dto.request.RegisterRequest;
import com.aluminibackend.api.module.auth.dto.response.AuthResponse;
import com.aluminibackend.api.module.auth.dto.response.CurrentUserResponse;
import com.aluminibackend.api.module.auth.security.JwtService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl service;

    // --- Helper Methods ---

    private User createUser(Long userId, String email, String empId, AccountType accountType, UserStatus status) {
        return User.builder()
                .userId(userId)
                .employeeId(empId)
                .name("Test User")
                .email(email)
                .phone("1234567890")
                .password("encodedPassword")
                .role(UserRole.EMPLOYEE)
                .accountType(accountType)
                .status(status)
                .build();
    }

    private RegisterRequest createRegisterRequest(String email, String empId, AccountType accountType,
            UserStatus status) {
        RegisterRequest request = new RegisterRequest();
        request.setEmployeeId(empId);
        request.setName("Test User");
        request.setEmail(email);
        request.setPhone("1234567890");
        request.setPassword("plainPassword");
        request.setAccountType(accountType);
        request.setStatus(status);
        return request;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // --- register Tests ---

    @Test
    void register_Success_WithCustomFields() {
        RegisterRequest request = createRegisterRequest("test@example.com", "EMP-001", AccountType.ALUMNI,
                UserStatus.ACTIVE);
        User savedUser = createUser(100L, "test@example.com", "EMP-001", AccountType.ALUMNI, UserStatus.ACTIVE);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByEmployeeId("EMP-001")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwtToken");

        AuthResponse response = service.register(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("EMP-001", response.getEmployeeId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(UserRole.EMPLOYEE, response.getRole());
        assertEquals(AccountType.ALUMNI, response.getAccountType());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("EMP-001", capturedUser.getEmployeeId());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals(AccountType.ALUMNI, capturedUser.getAccountType());
        assertEquals(UserStatus.ACTIVE, capturedUser.getStatus());

        verify(auditLogService).record("EMP-001", "REGISTER", "AUTH");
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void register_Success_WithDefaultFields() {
        RegisterRequest request = createRegisterRequest("test@example.com", "EMP-001", null, null);
        User savedUser = createUser(100L, "test@example.com", "EMP-001", AccountType.ACTIVE, UserStatus.ACTIVE);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByEmployeeId("EMP-001")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwtToken");

        AuthResponse response = service.register(request);

        assertNotNull(response);
        assertEquals(AccountType.ACTIVE, response.getAccountType());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(AccountType.ACTIVE, capturedUser.getAccountType());
        assertEquals(UserStatus.ACTIVE, capturedUser.getStatus());
    }

    @Test
    void register_ThrowsException_WhenEmailAlreadyExists() {
        RegisterRequest request = createRegisterRequest("test@example.com", "EMP-001", AccountType.ACTIVE,
                UserStatus.ACTIVE);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.register(request);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService, auditLogService);
    }

    @Test
    void register_ThrowsException_WhenEmployeeIdAlreadyExists() {
        RegisterRequest request = createRegisterRequest("test@example.com", "EMP-001", AccountType.ACTIVE,
                UserStatus.ACTIVE);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByEmployeeId("EMP-001")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.register(request);
        });

        assertEquals("Employee ID already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByEmployeeId("EMP-001");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService, auditLogService);
    }

    // --- login Tests ---

    @Test
    void login_Success() {
        LoginRequest request = createLoginRequest("test@example.com", "plainPassword");
        User user = createUser(100L, "test@example.com", "EMP-001", AccountType.ACTIVE, UserStatus.ACTIVE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthResponse response = service.login(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("EMP-001", response.getEmployeeId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(notificationService).createSystemNotification(
                "EMP-001",
                "Successful sign-in detected for your AlumniBridge account.",
                NotificationCategory.OFFBOARDING);
        verify(auditLogService).record("EMP-001", "LOGIN", "AUTH");
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_ThrowsException_WhenUserNotFound() {
        LoginRequest request = createLoginRequest("test@example.com", "plainPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.login(request);
        });

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(notificationService, auditLogService, jwtService);
    }

    @Test
    void login_ThrowsException_WhenUserInactive() {
        LoginRequest request = createLoginRequest("test@example.com", "plainPassword");
        User user = createUser(100L, "test@example.com", "EMP-001", AccountType.ACTIVE, UserStatus.INACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.login(request);
        });

        assertEquals("User account is inactive", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(notificationService, auditLogService, jwtService);
    }

    // --- getCurrentUser Tests ---

    @Test
    void getCurrentUser_Success() {
        User user = createUser(100L, "test@example.com", "EMP-001", AccountType.ACTIVE, UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CurrentUserResponse response = service.getCurrentUser("test@example.com");

        assertNotNull(response);
        assertEquals("EMP-001", response.getEmployeeId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhone());
        assertEquals(UserRole.EMPLOYEE, response.getRole());
        assertEquals(AccountType.ACTIVE, response.getAccountType());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_ThrowsException_WhenNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getCurrentUser("test@example.com");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
    }
}
