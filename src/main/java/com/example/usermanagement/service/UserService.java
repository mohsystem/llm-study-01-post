package com.example.usermanagement.service;

import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;

public interface UserService {

    RegistrationResponse register(RegistrationRequest request);
}
