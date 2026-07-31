package com.aluminibackend.api.module.user.dto.request;

import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    private UserRole role;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private AccountType accountType;

    private UserStatus status;
}