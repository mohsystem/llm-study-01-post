package com.example.usermanagement.controller;

import com.example.usermanagement.config.SecurityConfig;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.exception.GlobalExceptionHandler;
import com.example.usermanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    void registerShouldRejectWeakPassword() throws Exception {
        RegistrationRequest request = new RegistrationRequest("secure_user", "secure@example.com", "weakpass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
