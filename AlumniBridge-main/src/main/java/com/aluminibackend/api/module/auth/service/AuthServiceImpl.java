package com.aluminibackend.api.module.auth.service;

import com.aluminibackend.api.module.auth.dto.request.LoginRequest;
import com.aluminibackend.api.module.auth.dto.request.RegisterRequest;
import com.aluminibackend.api.module.auth.dto.response.AuthResponse;
import com.aluminibackend.api.module.auth.dto.response.CurrentUserResponse;
import com.aluminibackend.api.module.auth.security.JwtService;
import com.aluminibackend.api.module.audit.service.AuditLogService;
import com.aluminibackend.api.module.notification.enums.NotificationCategory;
import com.aluminibackend.api.module.notification.service.NotificationService;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.EMPLOYEE)
                .accountType(
                        request.getAccountType() != null
                                ? request.getAccountType()
                                : AccountType.ACTIVE
                )
                .status(
                        request.getStatus() != null
                                ? request.getStatus()
                                : UserStatus.ACTIVE
                )
                .build();

        User savedUser = userRepository.save(user);
        auditLogService.record(savedUser.getEmployeeId(), "REGISTER", "AUTH");

        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("User account is inactive");
        }

        notificationService.createSystemNotification(
                user.getEmployeeId(),
                "Successful sign-in detected for your AlumniBridge account.",
                NotificationCategory.OFFBOARDING
        );

        auditLogService.record(user.getEmployeeId(), "LOGIN", "AUTH");

        String token = jwtService.generateToken(user);

        return buildAuthResponse(user, token);
    }

    @Override
    public CurrentUserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return CurrentUserResponse.builder()
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accountType(user.getAccountType())
                .status(user.getStatus())
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String token) {

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accountType(user.getAccountType())
                .status(user.getStatus())
                .build();
    }
}
