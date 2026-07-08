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
import com.vivekai.studio.exception.RefreshTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @Value("${vivekai.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${vivekai.jwt.refreshExpirationMs:604800000}")
    private long refreshExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        log.info("Received login request for username: {}", loginRequest.getUsername());
        
        // Populate authentic client headers to prevent spoofing
        loginRequest.setIpAddress(servletRequest.getRemoteAddr());
        loginRequest.setUserAgent(servletRequest.getHeader("User-Agent"));

        AuthResponse authResponse = authService.login(loginRequest);
        
        // Write refreshToken to HttpOnly cookie and remove from response JSON body
        setRefreshTokenCookie(servletResponse, authResponse.getRefreshToken());
        authResponse.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshAuthentication(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) TokenRefreshRequest refreshRequest,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        log.info("Received refresh token request");
        
        String tokenToUse = cookieRefreshToken;
        if (tokenToUse == null && refreshRequest != null) {
            tokenToUse = refreshRequest.getRefreshToken();
        }

        if (tokenToUse == null || tokenToUse.trim().isEmpty()) {
            throw new RefreshTokenException("Refresh token is missing");
        }

        // Prepare request details using authentic headers
        String deviceName = refreshRequest != null ? refreshRequest.getDeviceName() : null;
        String deviceType = refreshRequest != null ? refreshRequest.getDeviceType() : null;
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        TokenRefreshRequest resolvedRequest = TokenRefreshRequest.builder()
                .refreshToken(tokenToUse)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        AuthResponse authResponse = authService.refresh(resolvedRequest);
        
        // Rotate the HttpOnly cookie and remove from response JSON body
        setRefreshTokenCookie(servletResponse, authResponse.getRefreshToken());
        authResponse.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse servletResponse
    ) {
        log.info("Received logout request");
        if (cookieRefreshToken != null) {
            authService.logout(cookieRefreshToken);
        }
        
        // Invalidate the client's HttpOnly cookie by setting Max-Age=0
        String cookieHeader = "refreshToken=; Path=/api/v1/auth; Max-Age=0; HttpOnly; SameSite=Strict";
        if (cookieSecure) {
            cookieHeader += "; Secure";
        }
        servletResponse.setHeader("Set-Cookie", cookieHeader);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        String cookieHeader = String.format("refreshToken=%s; Path=/api/v1/auth; Max-Age=%d; HttpOnly; SameSite=Strict", 
                refreshToken, refreshExpirationMs / 1000);
        if (cookieSecure) {
            cookieHeader += "; Secure";
        }
        response.setHeader("Set-Cookie", cookieHeader);
    }
}
