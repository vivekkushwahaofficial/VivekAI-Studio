package com.vivekai.studio.auth.controller;

import com.vivekai.studio.auth.dto.AuthResponse;
import com.vivekai.studio.auth.dto.LoginRequest;
import com.vivekai.studio.auth.dto.RegisterRequest;
import com.vivekai.studio.auth.dto.TokenRefreshRequest;
import com.vivekai.studio.auth.dto.UserResponse;
import com.vivekai.studio.auth.service.AuthService;
import com.vivekai.studio.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.vivekai.studio.security.service.UserDetailsImpl;
import com.vivekai.studio.user.repository.UserRepository;
import com.vivekai.studio.auth.mapper.UserMapper;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Fetching profile details for authenticated user: {}", userDetails.getUsername());
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user), "User details retrieved"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for username: {}", registerRequest.getUsername());
        UserResponse userResponse = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userResponse, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for username: {}", loginRequest.getUsername());
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshAuthentication(
            @Valid @RequestBody TokenRefreshRequest refreshRequest) {
        log.info("Received refresh token request");
        AuthResponse authResponse = authService.refresh(refreshRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }
}
