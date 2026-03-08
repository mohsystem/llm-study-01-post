package com.example.usermanagement.service;

import com.example.usermanagement.dto.PasswordRulesRequest;
import com.example.usermanagement.dto.PasswordRulesResponse;

public interface PasswordRulesService {

    PasswordRulesResponse getRules();

    PasswordRulesResponse updateRules(PasswordRulesRequest request);

    void validateOrThrow(String password);
}
