package com.example.usermanagement.service;

import com.example.usermanagement.config.AuthFlowProperties;
import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.MfaChallengeRequest;
import com.example.usermanagement.dto.MfaChallengeResponse;
import com.example.usermanagement.dto.MfaVerifyRequest;
import com.example.usermanagement.dto.MfaVerifyResponse;
import com.example.usermanagement.dto.ResetConfirmRequest;
import com.example.usermanagement.dto.ResetRequest;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.entity.MfaOtpChallenge;
import com.example.usermanagement.entity.PasswordResetToken;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.exception.InvalidSessionException;
import com.example.usermanagement.exception.TooManyAttemptsException;
import com.example.usermanagement.repository.MfaOtpChallengeRepository;
import com.example.usermanagement.repository.PasswordResetTokenRepository;
import com.example.usermanagement.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MfaOtpChallengeRepository mfaOtpChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordRulesService passwordRulesService;
    private final NotificationClient notificationClient;
    private final AuthFlowProperties authFlowProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountSecurityServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            MfaOtpChallengeRepository mfaOtpChallengeRepository,
            PasswordEncoder passwordEncoder,
            PasswordRulesService passwordRulesService,
            NotificationClient notificationClient,
            AuthFlowProperties authFlowProperties
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mfaOtpChallengeRepository = mfaOtpChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordRulesService = passwordRulesService;
        this.notificationClient = notificationClient;
        this.authFlowProperties = authFlowProperties;
    }

    @Override
    @Transactional
    public StatusResponse changePassword(Jwt jwt, ChangePasswordRequest request) {
        User user = loadUserFromJwt(jwt);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        passwordRulesService.validateOrThrow(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return new StatusResponse("ACCEPTED", true);
    }

    @Override
    @Transactional
    public StatusResponse requestPasswordReset(ResetRequest request) {
        String identity = request.identifier().trim();
        String emailIdentity = identity.toLowerCase(Locale.ROOT);

        userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identity, emailIdentity).ifPresent(user -> {
            String rawToken = generateResetToken();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getId());
            resetToken.setTokenHash(sha256(rawToken));
            resetToken.setExpiresAt(Instant.now().plusSeconds(authFlowProperties.resetTokenTtlMinutes() * 60));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);
        });

        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
        return new StatusResponse("ACCEPTED", true);
    }

    @Override
    @Transactional
    public StatusResponse confirmPasswordReset(ResetConfirmRequest request) {
        passwordRulesService.validateOrThrow(request.newPassword());
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(sha256(request.resetToken()))
                .orElseThrow(() -> new InvalidSessionException("Invalid session"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidSessionException("Invalid session");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new InvalidSessionException("Invalid session"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        return new StatusResponse("ACCEPTED", true);
    }

    @Override
    @Transactional
    public MfaChallengeResponse challengeMfa(Jwt jwt, MfaChallengeRequest request) {
        User user = loadUserFromJwt(jwt);

        String otp = generateOtp();
        MfaOtpChallenge challenge = new MfaOtpChallenge();
        challenge.setUserId(user.getId());
        challenge.setOtpHash(sha256(otp));
        challenge.setExpiresAt(Instant.now().plusSeconds(authFlowProperties.otpTtlMinutes() * 60));
        challenge.setAttempts(0);
        challenge.setVerified(false);
        challenge.setLocked(false);

        MfaOtpChallenge saved = mfaOtpChallengeRepository.save(challenge);
        notificationClient.sendOtp(request.destination(), otp);
        mfaOtpChallengeRepository.deleteByExpiresAtBefore(Instant.now());

        return new MfaChallengeResponse(saved.getId(), "CHALLENGE_SENT");
    }

    @Override
    @Transactional
    public MfaVerifyResponse verifyMfa(Jwt jwt, MfaVerifyRequest request) {
        User user = loadUserFromJwt(jwt);
        MfaOtpChallenge challenge = mfaOtpChallengeRepository.findByIdAndUserId(request.challengeId(), user.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (challenge.isLocked() || challenge.getExpiresAt().isBefore(Instant.now()) || challenge.isVerified()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!challenge.getOtpHash().equals(sha256(request.otp()))) {
            int attempts = challenge.getAttempts() + 1;
            challenge.setAttempts(attempts);
            if (attempts >= authFlowProperties.otpMaxAttempts()) {
                challenge.setLocked(true);
                mfaOtpChallengeRepository.save(challenge);
                throw new TooManyAttemptsException("Too many attempts");
            }
            mfaOtpChallengeRepository.save(challenge);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        challenge.setVerified(true);
        mfaOtpChallengeRepository.save(challenge);
        return new MfaVerifyResponse("MFA_VERIFIED", true);
    }

    private User loadUserFromJwt(Jwt jwt) {
        Long userId = jwt.getClaim("uid");
        if (userId == null) {
            throw new InvalidSessionException("Invalid session");
        }
        return userRepository.findById(userId).orElseThrow(() -> new InvalidSessionException("Invalid session"));
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
