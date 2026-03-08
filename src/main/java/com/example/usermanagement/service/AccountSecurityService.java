package com.example.usermanagement.service;

import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.MfaChallengeRequest;
import com.example.usermanagement.dto.MfaChallengeResponse;
import com.example.usermanagement.dto.MfaVerifyRequest;
import com.example.usermanagement.dto.MfaVerifyResponse;
import com.example.usermanagement.dto.ResetConfirmRequest;
import com.example.usermanagement.dto.ResetRequest;
import com.example.usermanagement.dto.StatusResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AccountSecurityService {

    StatusResponse changePassword(Jwt jwt, ChangePasswordRequest request);

    StatusResponse requestPasswordReset(ResetRequest request);

    StatusResponse confirmPasswordReset(ResetConfirmRequest request);

    MfaChallengeResponse challengeMfa(Jwt jwt, MfaChallengeRequest request);

    MfaVerifyResponse verifyMfa(Jwt jwt, MfaVerifyRequest request);
}
