package com.aluminibackend.api.module.user.dto.response;

import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private String employeeId;

    private String name;

    private UserRole role;

    private String email;

    private String phone;

    private AccountType accountType;

    private UserStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}