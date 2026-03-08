package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {

    RegistrationResponse register(RegistrationRequest request);

    LoginResponse login(LoginRequest request);

    RefreshResponse refresh(Jwt jwt);

    LogoutResponse logout(Jwt jwt);
}
