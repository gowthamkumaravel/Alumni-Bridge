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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserByEmployeeId(String employeeId) {
        return userMapper.toResponse(findUserByEmployeeId(employeeId));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(String employeeId, UpdateUserRequest request) {

        User user = findUserByEmployeeId(employeeId);

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        userMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(String employeeId) {
        User user = findUserByEmployeeId(employeeId);
        userRepository.delete(user);
    }

    @Override
    public UserResponse deactivateUser(String employeeId) {
        User user = findUserByEmployeeId(employeeId);
        user.setStatus(UserStatus.INACTIVE);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse convertToAlumni(String employeeId) {
        User user = findUserByEmployeeId(employeeId);

        user.setAccountType(AccountType.ALUMNI);
        user.setStatus(UserStatus.ALUMNI);
        user.setRole(UserRole.EMPLOYEE);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    private User findUserByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("User not found with employee ID: " + employeeId));
    }
}