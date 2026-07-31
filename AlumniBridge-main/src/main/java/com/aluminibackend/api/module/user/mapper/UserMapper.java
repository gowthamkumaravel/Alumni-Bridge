package com.aluminibackend.api.module.user.mapper;

import com.aluminibackend.api.module.user.dto.request.CreateUserRequest;
import com.aluminibackend.api.module.user.dto.request.UpdateUserRequest;
import com.aluminibackend.api.module.user.dto.response.UserResponse;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    //User mapper password encoder not done
    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .role(request.getRole())
                .email(request.getEmail())
                .phone(request.getPhone())
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
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .role(user.getRole())
                .email(user.getEmail())
                .phone(user.getPhone())
                .accountType(user.getAccountType())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public void updateEntity(User user, UpdateUserRequest request) {

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAccountType() != null) {
            user.setAccountType(request.getAccountType());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
    }
}