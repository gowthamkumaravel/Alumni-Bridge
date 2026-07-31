package com.aluminibackend.api.module.user.controller;

import com.aluminibackend.api.module.user.dto.request.CreateUserRequest;
import com.aluminibackend.api.module.user.dto.request.UpdateUserRequest;
import com.aluminibackend.api.module.user.dto.response.UserResponse;
import com.aluminibackend.api.module.user.enums.UserRole;
import com.aluminibackend.api.module.user.enums.UserStatus;
import com.aluminibackend.api.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/{employeeId}")
    public ResponseEntity<UserResponse> getUserByEmployeeId(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(userService.getUserByEmployeeId(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable UserRole role
    ) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(
            @PathVariable UserStatus status
    ) {
        return ResponseEntity.ok(userService.getUsersByStatus(status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{employeeId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String employeeId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(employeeId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PatchMapping("/{employeeId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(userService.deactivateUser(employeeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HRBP')")
    @PatchMapping("/{employeeId}/convert-to-alumni")
    public ResponseEntity<UserResponse> convertToAlumni(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(userService.convertToAlumni(employeeId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String employeeId
    ) {
        userService.deleteUser(employeeId);
        return ResponseEntity.noContent().build();
    }
}
