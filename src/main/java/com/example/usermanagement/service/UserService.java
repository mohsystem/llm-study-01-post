package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;

public interface UserService {

    RegistrationResponse register(RegistrationRequest request);

    LoginResponse login(LoginRequest request);
}
