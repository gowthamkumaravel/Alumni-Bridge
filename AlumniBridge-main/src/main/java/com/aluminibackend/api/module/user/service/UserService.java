package com.aluminibackend.api.module.user.service;

import com.aluminibackend.api.module.user.dto.request.CreateUserRequest;
import com.aluminibackend.api.module.user.dto.request.UpdateUserRequest;
import com.aluminibackend.api.module.user.dto.response.UserResponse;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserByEmployeeId(String employeeId);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(UserRole role);

    List<UserResponse> getUsersByStatus(UserStatus status);

    UserResponse updateUser(String employeeId, UpdateUserRequest request);

    void deleteUser(String employeeId);

    UserResponse deactivateUser(String employeeId);

    UserResponse convertToAlumni(String employeeId);
}