package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiKeyCreateRequest;
import com.example.usermanagement.dto.ApiKeyCreateResponse;
import com.example.usermanagement.dto.ApiKeyResponse;
import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.MfaChallengeRequest;
import com.example.usermanagement.dto.MfaChallengeResponse;
import com.example.usermanagement.dto.MfaVerifyRequest;
import com.example.usermanagement.dto.MfaVerifyResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.ResetConfirmRequest;
import com.example.usermanagement.dto.ResetRequest;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.service.AccountSecurityService;
import com.example.usermanagement.service.ApiKeyService;
import com.example.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AccountSecurityService accountSecurityService;
    private final ApiKeyService apiKeyService;

    public AuthController(UserService userService, AccountSecurityService accountSecurityService, ApiKeyService apiKeyService) {
        this.userService = userService;
        this.accountSecurityService = accountSecurityService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@AuthenticationPrincipal Jwt jwt) {
        RefreshResponse response = userService.refresh(jwt);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal Jwt jwt) {
        LogoutResponse response = userService.logout(jwt);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApiKeyCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createKey(jwt, request));
    }

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(apiKeyService.listKeys(jwt));
    }

    @DeleteMapping("/api-keys/{keyId}")
    public ResponseEntity<StatusResponse> deleteApiKey(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String keyId
    ) {
        return ResponseEntity.ok(apiKeyService.revokeKey(jwt, keyId));
    }

    @PostMapping("/change-password")
    public ResponseEntity<StatusResponse> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(accountSecurityService.changePassword(jwt, request));
    }

    @PostMapping("/reset-request")
    public ResponseEntity<StatusResponse> resetRequest(@Valid @RequestBody ResetRequest request) {
        return ResponseEntity.ok(accountSecurityService.requestPasswordReset(request));
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<StatusResponse> resetConfirm(@Valid @RequestBody ResetConfirmRequest request) {
        return ResponseEntity.ok(accountSecurityService.confirmPasswordReset(request));
    }

    @PostMapping("/mfa/challenge")
    public ResponseEntity<MfaChallengeResponse> mfaChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MfaChallengeRequest request
    ) {
        return ResponseEntity.ok(accountSecurityService.challengeMfa(jwt, request));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<MfaVerifyResponse> mfaVerify(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MfaVerifyRequest request
    ) {
        return ResponseEntity.ok(accountSecurityService.verifyMfa(jwt, request));
    }
}
