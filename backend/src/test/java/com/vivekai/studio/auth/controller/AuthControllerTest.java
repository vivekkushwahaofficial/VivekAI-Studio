package com.vivekai.studio.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivekai.studio.auth.dto.AuthResponse;
import com.vivekai.studio.auth.dto.LoginRequest;
import com.vivekai.studio.auth.dto.RegisterRequest;
import com.vivekai.studio.auth.dto.UserResponse;
import com.vivekai.studio.auth.service.AuthService;
import com.vivekai.studio.security.jwt.JwtService;
import com.vivekai.studio.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerUser_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@vivekai.com")
                .password("password123")
                .build();

        UserResponse response = UserResponse.builder()
                .username("testuser")
                .email("test@vivekai.com")
                .status("PENDING_VERIFICATION")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@vivekai.com"));
    }

    @Test
    public void authenticateUser_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .username("testuser")
                .email("test@vivekai.com")
                .status("ACTIVE")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mockAccessToken")
                .refreshToken("mockRefreshToken")
                .user(userResponse)
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist()); // Confirms hidden from payload body
    }
}
