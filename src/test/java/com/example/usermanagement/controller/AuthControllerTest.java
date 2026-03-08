package com.example.usermanagement.controller;

import com.example.usermanagement.config.SecurityConfig;
import com.example.usermanagement.dto.ApiKeyCreateRequest;
import com.example.usermanagement.dto.ApiKeyCreateResponse;
import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.exception.GlobalExceptionHandler;
import com.example.usermanagement.service.AccountSecurityService;
import com.example.usermanagement.service.ApiKeyService;
import com.example.usermanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private AccountSecurityService accountSecurityService;

    @MockBean
    private ApiKeyService apiKeyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void registerShouldReturnCreated() throws Exception {
        given(userService.register(any(RegistrationRequest.class)))
                .willReturn(new RegistrationResponse(10L, "REGISTERED"));

        RegistrationRequest request = new RegistrationRequest("secure_user", "secure@example.com", "+15551234567", "StrongPass!123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10L));
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
    void apiKeyCreateShouldReturnCreated() throws Exception {
        given(apiKeyService.createKey(any(), any(ApiKeyCreateRequest.class)))
                .willReturn(new ApiKeyCreateResponse("kid-1", "ak_kid-1.secret", "ACCEPTED", true));

        ApiKeyCreateRequest request = new ApiKeyCreateRequest("integration-key", "", 30);

        mockMvc.perform(post("/api/auth/api-keys")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void apiKeyListShouldReturnKeys() throws Exception {
        given(apiKeyService.listKeys(any())).willReturn(List.of());

        mockMvc.perform(get("/api/auth/api-keys")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void apiKeyDeleteShouldReturnAccepted() throws Exception {
        given(apiKeyService.revokeKey(any(), any())).willReturn(new StatusResponse("ACCEPTED", true));

        mockMvc.perform(delete("/api/auth/api-keys/kid-1")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void changePasswordShouldReturnAccepted() throws Exception {
        given(accountSecurityService.changePassword(any(), any()))
                .willReturn(new StatusResponse("ACCEPTED", true));

        mockMvc.perform(post("/api/auth/change-password")
                        .with(jwt().jwt(j -> j.subject("secure_user").claim("uid", 1L).id("jti-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("StrongPass!123", "NewStrongPass!123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
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
