package com.aluminibackend.api.module.auth.dto.response;

import com.aluminibackend.api.module.user.enums.AccountType;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {

    private String employeeId;

    private String name;

    private String email;

    private String phone;

    private UserRole role;

    private AccountType accountType;

    private UserStatus status;
}