package com.vivekai.studio.auth.service;

import com.vivekai.studio.auth.dto.AuthResponse;
import com.vivekai.studio.auth.dto.LoginRequest;
import com.vivekai.studio.auth.dto.RegisterRequest;
import com.vivekai.studio.auth.dto.TokenRefreshRequest;
import com.vivekai.studio.auth.dto.UserResponse;
import com.vivekai.studio.auth.entity.RefreshToken;
import com.vivekai.studio.auth.mapper.UserMapper;
import com.vivekai.studio.exception.InvalidCredentialsException;
import com.vivekai.studio.exception.RefreshTokenException;
import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.security.jwt.JwtService;
import com.vivekai.studio.security.service.UserDetailsImpl;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());
        User user = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
        return userMapper.toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtService.generateToken(userDetails);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    userDetails.getId(),
                    request.getDeviceName(),
                    request.getDeviceType(),
                    request.getIpAddress(),
                    request.getUserAgent()
            );

            UserResponse userResponse = UserResponse.builder()
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .status(userDetails.getStatus().name())
                    .roles(userDetails.getAuthorities().stream()
                            .map(item -> item.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();

            return AuthResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken.getToken())
                    .user(userResponse)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed authentication attempt for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                    String token = jwtService.generateToken(userDetails);
                    
                    // Revoke old token and create new rotated refresh token
                    refreshTokenService.revokeToken(requestRefreshToken);
                    
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                            user.getId(),
                            request.getDeviceName(),
                            request.getDeviceType(),
                            request.getIpAddress(),
                            request.getUserAgent()
                    );

                    return AuthResponse.builder()
                            .accessToken(token)
                            .refreshToken(newRefreshToken.getToken())
                            .user(userMapper.toResponse(user))
                            .build();
                })
                .orElseThrow(() -> new RefreshTokenException("Refresh token is not in database. Please log in again."));
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("Revoking refresh token on logout");
        try {
            refreshTokenService.revokeToken(refreshToken);
        } catch (ResourceNotFoundException e) {
            log.warn("Logout requested with non-existent refresh token");
        }
    }
}
