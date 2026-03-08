package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.entity.RevokedToken;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.exception.InvalidSessionException;
import com.example.usermanagement.repository.RevokedTokenRepository;
import com.example.usermanagement.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserServiceImpl(
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            User saved = userRepository.save(user);
            return new RegistrationResponse(saved.getId(), "REGISTERED");
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Username or email already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedIdentity = request.usernameOrEmail().trim();
        String normalizedEmailIdentity = normalizedIdentity.toLowerCase(Locale.ROOT);

        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedIdentity, normalizedEmailIdentity)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = tokenService.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token);
    }

    @Override
    @Transactional
    public RefreshResponse refresh(Jwt jwt) {
        revokeToken(jwt);
        Long userId = jwt.getClaim("uid");
        if (userId == null) {
            throw new InvalidSessionException("Invalid session");
        }

        String newToken = tokenService.generateToken(userId, jwt.getSubject());
        return new RefreshResponse(newToken);
    }

    @Override
    @Transactional
    public LogoutResponse logout(Jwt jwt) {
        revokeToken(jwt);
        return new LogoutResponse("LOGGED_OUT");
    }

    private void revokeToken(Jwt jwt) {
        String jti = jwt.getId();
        if (jti == null || jti.isBlank()) {
            throw new InvalidSessionException("Invalid session");
        }

        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new InvalidSessionException("Invalid session");
        }

        if (!revokedTokenRepository.existsByJti(jti)) {
            RevokedToken revokedToken = new RevokedToken();
            revokedToken.setJti(jti);
            revokedToken.setExpiresAt(expiresAt);
            revokedTokenRepository.save(revokedToken);
        }

        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
