package com.example.usermanagement.controller;

import com.example.usermanagement.dto.PasswordRulesRequest;
import com.example.usermanagement.dto.PasswordRulesResponse;
import com.example.usermanagement.service.PasswordRulesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts/password-rules")
public class AdminPasswordRulesController {

    private final PasswordRulesService passwordRulesService;

    public AdminPasswordRulesController(PasswordRulesService passwordRulesService) {
        this.passwordRulesService = passwordRulesService;
    }

    @GetMapping
    public ResponseEntity<PasswordRulesResponse> getRules() {
        return ResponseEntity.ok(passwordRulesService.getRules());
    }

    @PutMapping
    public ResponseEntity<PasswordRulesResponse> updateRules(@Valid @RequestBody PasswordRulesRequest request) {
        return ResponseEntity.ok(passwordRulesService.updateRules(request));
    }
}
