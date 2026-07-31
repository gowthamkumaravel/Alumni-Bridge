package com.aluminibackend.api.module.auth.service;

import com.aluminibackend.api.module.auth.dto.request.LoginRequest;
import com.aluminibackend.api.module.auth.dto.request.RegisterRequest;
import com.aluminibackend.api.module.auth.dto.response.AuthResponse;
import com.aluminibackend.api.module.auth.dto.response.CurrentUserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    CurrentUserResponse getCurrentUser(String email);
}