package com.example.usermanagement.controller;

import com.example.usermanagement.config.SecurityConfig;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.exception.GlobalExceptionHandler;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void registerShouldReturnCreated() throws Exception {
        given(userService.register(any(RegistrationRequest.class)))
                .willReturn(new RegistrationResponse(10L, "REGISTERED"));

        RegistrationRequest request = new RegistrationRequest("secure_user", "secure@example.com", "StrongPass!123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10L))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        given(userService.login(any(LoginRequest.class))).willReturn(new LoginResponse("jwt-token"));

        LoginRequest request = new LoginRequest("secure_user", "StrongPass!123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value("jwt-token"));
    }

    @Test
    void loginShouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        given(userService.login(any(LoginRequest.class))).willThrow(new InvalidCredentialsException("Invalid credentials"));

        LoginRequest request = new LoginRequest("secure_user", "WrongPass!123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshShouldReturnNewToken() throws Exception {
        given(userService.refresh(any())).willReturn(new RefreshResponse("new-jwt-token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value("new-jwt-token"));
    }

    @Test
    void logoutShouldReturnConfirmation() throws Exception {
        given(userService.logout(any())).willReturn(new LogoutResponse("LOGGED_OUT"));

        mockMvc.perform(post("/api/auth/logout")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOGGED_OUT"));
    }
}
