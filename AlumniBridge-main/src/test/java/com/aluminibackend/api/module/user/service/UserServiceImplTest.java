package com.aluminibackend.api.module.user.service;

import com.aluminibackend.api.module.user.dto.request.CreateUserRequest;
import com.aluminibackend.api.module.user.dto.request.UpdateUserRequest;
import com.aluminibackend.api.module.user.dto.response.UserResponse;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.mapper.UserMapper;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // --- createUser tests ---

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setEmployeeId("EMP123");
        request.setPassword("rawPassword");

        User user = User.builder()
                .email("test@example.com")
                .employeeId("EMP123")
                .build();

        User savedUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .employeeId("EMP123")
                .password("encodedPassword")
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .employeeId("EMP123")
                .email("test@example.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByEmployeeId(request.getEmployeeId())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("EMP123", response.getEmployeeId());
        assertEquals("test@example.com", response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByEmployeeId(request.getEmployeeId());
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode("rawPassword");
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void createUser_ThrowsException_WhenEmailExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(request);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    void createUser_ThrowsException_WhenEmployeeIdExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setEmployeeId("EMP123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByEmployeeId(request.getEmployeeId())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(request);
        });

        assertEquals("Employee ID already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
    }

    // --- getUserByEmployeeId tests ---

    @Test
    void getUserByEmployeeId_Success() {
        // Arrange
        String employeeId = "EMP123";
        User user = User.builder().employeeId(employeeId).build();
        UserResponse expectedResponse = UserResponse.builder().employeeId(employeeId).build();

        // Using doReturn to satisfy the requirement of using doReturn
        doReturn(Optional.of(user)).when(userRepository).findByEmployeeId(employeeId);
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.getUserByEmployeeId(employeeId);

        // Assert
        assertNotNull(response);
        assertEquals(employeeId, response.getEmployeeId());
        verify(userRepository).findByEmployeeId(employeeId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserByEmployeeId_ThrowsException_WhenUserNotFound() {
        // Arrange
        String employeeId = "EMP123";
        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByEmployeeId(employeeId);
        });

        assertEquals("User not found with employee ID: EMP123", exception.getMessage());
        verify(userMapper, never()).toResponse(any());
    }

    // --- getAllUsers tests ---

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user = User.builder().employeeId("EMP001").build();
        UserResponse userResponse = UserResponse.builder().employeeId("EMP001").build();

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("EMP001", responses.get(0).getEmployeeId());
        verify(userRepository).findAll();
        verify(userMapper).toResponse(user);
    }

    // --- getUsersByRole tests ---

    @Test
    void getUsersByRole_Success() {
        // Arrange
        UserRole role = UserRole.EMPLOYEE;
        User user = User.builder().employeeId("EMP001").role(role).build();
        UserResponse userResponse = UserResponse.builder().employeeId("EMP001").role(role).build();

        when(userRepository.findByRole(role)).thenReturn(Collections.singletonList(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        List<UserResponse> responses = userService.getUsersByRole(role);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(role, responses.get(0).getRole());
        verify(userRepository).findByRole(role);
    }

    // --- getUsersByStatus tests ---

    @Test
    void getUsersByStatus_Success() {
        // Arrange
        UserStatus status = UserStatus.ACTIVE;
        User user = User.builder().employeeId("EMP001").status(status).build();
        UserResponse userResponse = UserResponse.builder().employeeId("EMP001").status(status).build();

        when(userRepository.findByStatus(status)).thenReturn(Collections.singletonList(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        List<UserResponse> responses = userService.getUsersByStatus(status);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(status, responses.get(0).getStatus());
        verify(userRepository).findByStatus(status);
    }

    // --- updateUser tests ---

    @Test
    void updateUser_Success_WithNewEmail() {
        // Arrange
        String employeeId = "EMP001";
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");

        User existingUser = User.builder()
                .employeeId(employeeId)
                .email("old@example.com")
                .build();

        User updatedUser = User.builder()
                .employeeId(employeeId)
                .email("new@example.com")
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .employeeId(employeeId)
                .email("new@example.com")
                .build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        doNothing().when(userMapper).updateEntity(existingUser, request);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.updateUser(employeeId, request);

        // Assert
        assertNotNull(response);
        assertEquals("new@example.com", response.getEmail());
        verify(userRepository).findByEmployeeId(employeeId);
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userMapper).updateEntity(existingUser, request);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_Success_WithSameEmail() {
        // Arrange
        String employeeId = "EMP001";
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("same@example.com");

        User existingUser = User.builder()
                .employeeId(employeeId)
                .email("same@example.com")
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .employeeId(employeeId)
                .email("same@example.com")
                .build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userMapper).updateEntity(existingUser, request);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.updateUser(employeeId, request);

        // Assert
        assertNotNull(response);
        assertEquals("same@example.com", response.getEmail());
        verify(userRepository).findByEmployeeId(employeeId);
        verify(userRepository, never()).existsByEmail(any());
        verify(userMapper).updateEntity(existingUser, request);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_ThrowsException_WhenEmailExists() {
        // Arrange
        String employeeId = "EMP001";
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("exists@example.com");

        User existingUser = User.builder()
                .employeeId(employeeId)
                .email("old@example.com")
                .build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(employeeId, request);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ThrowsException_WhenUserNotFound() {
        // Arrange
        String employeeId = "EMP001";
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(employeeId, request);
        });

        verify(userRepository, never()).save(any());
    }

    // --- deleteUser tests ---

    @Test
    void deleteUser_Success() {
        // Arrange
        String employeeId = "EMP001";
        User user = User.builder().employeeId(employeeId).build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(employeeId);

        // Assert
        verify(userRepository).findByEmployeeId(employeeId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        // Arrange
        String employeeId = "EMP001";
        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(employeeId);
        });

        verify(userRepository, never()).delete(any());
    }

    // --- deactivateUser tests ---

    @Test
    void deactivateUser_Success() {
        // Arrange
        String employeeId = "EMP001";
        User user = User.builder()
                .employeeId(employeeId)
                .status(UserStatus.ACTIVE)
                .build();

        User deactivatedUser = User.builder()
                .employeeId(employeeId)
                .status(UserStatus.INACTIVE)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .employeeId(employeeId)
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(deactivatedUser);
        when(userMapper.toResponse(deactivatedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.deactivateUser(employeeId);

        // Assert
        assertNotNull(response);
        assertEquals(UserStatus.INACTIVE, response.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserStatus.INACTIVE, userCaptor.getValue().getStatus());

        verify(userRepository).findByEmployeeId(employeeId);
    }

    @Test
    void deactivateUser_ThrowsException_WhenUserNotFound() {
        // Arrange
        String employeeId = "EMP001";
        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.deactivateUser(employeeId);
        });

        verify(userRepository, never()).save(any());
    }

    // --- convertToAlumni tests ---

    @Test
    void convertToAlumni_Success() {
        // Arrange
        String employeeId = "EMP001";
        User user = User.builder()
                .employeeId(employeeId)
                .accountType(AccountType.ACTIVE)
                .status(UserStatus.ACTIVE)
                .role(UserRole.EMPLOYEE)
                .build();

        User alumniUser = User.builder()
                .employeeId(employeeId)
                .accountType(AccountType.ALUMNI)
                .status(UserStatus.ALUMNI)
                .role(UserRole.EMPLOYEE)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .employeeId(employeeId)
                .accountType(AccountType.ALUMNI)
                .status(UserStatus.ALUMNI)
                .role(UserRole.EMPLOYEE)
                .build();

        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(alumniUser);
        when(userMapper.toResponse(alumniUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.convertToAlumni(employeeId);

        // Assert
        assertNotNull(response);
        assertEquals(AccountType.ALUMNI, response.getAccountType());
        assertEquals(UserStatus.ALUMNI, response.getStatus());
        assertEquals(UserRole.EMPLOYEE, response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(AccountType.ALUMNI, capturedUser.getAccountType());
        assertEquals(UserStatus.ALUMNI, capturedUser.getStatus());
        assertEquals(UserRole.EMPLOYEE, capturedUser.getRole());

        verify(userRepository).findByEmployeeId(employeeId);
    }

    @Test
    void convertToAlumni_ThrowsException_WhenUserNotFound() {
        // Arrange
        String employeeId = "EMP001";
        when(userRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.convertToAlumni(employeeId);
        });

        verify(userRepository, never()).save(any());
    }
}
