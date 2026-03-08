package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.repository.RevokedTokenRepository;
import com.example.usermanagement.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.TestConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({UserServiceImpl.class, UserServiceImplTest.TestSecurityConfig.class})
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerShouldCreateUserWithHashedPassword() {
        RegistrationRequest request = new RegistrationRequest("secure_user", "secure@example.com", "+15551234567", "StrongPass!123");

        RegistrationResponse response = userService.register(request);

        assertThat(response.accountId()).isNotNull();
        var created = userRepository.findByEmailIgnoreCase("secure@example.com").orElseThrow();
        assertThat(created.getPasswordHash()).isNotEqualTo("StrongPass!123");
        assertThat(passwordEncoder.matches("StrongPass!123", created.getPasswordHash())).isTrue();
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        userService.register(new RegistrationRequest("user_one", "duplicate@example.com", null, "StrongPass!123"));

        assertThatThrownBy(() -> userService.register(
                new RegistrationRequest("user_two", "duplicate@example.com", null, "StrongPass!123")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        userService.register(new RegistrationRequest("login_user", "login@example.com", null, "StrongPass!123"));
        LoginResponse response = userService.login(new LoginRequest("login_user", "StrongPass!123"));
        assertThat(response.sessionToken()).isEqualTo("test-token-login_user");
    }

    @Test
    void loginShouldRejectInvalidPasswordWithoutEnumeration() {
        userService.register(new RegistrationRequest("login_user2", "login2@example.com", null, "StrongPass!123"));

        assertThatThrownBy(() -> userService.login(new LoginRequest("login_user2", "WrongPass!123")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThatThrownBy(() -> userService.login(new LoginRequest("unknown_user", "WrongPass!123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshShouldRevokeCurrentTokenAndIssueNewToken() {
        Jwt current = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("secure_user")
                .claim("uid", 44L)
                .id("jti-current")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();

        RefreshResponse response = userService.refresh(current);

        assertThat(response.sessionToken()).isEqualTo("test-token-secure_user");
        assertThat(revokedTokenRepository.existsByJti("jti-current")).isTrue();
    }

    @Test
    void logoutShouldRevokeCurrentToken() {
        Jwt current = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("secure_user")
                .claim("uid", 44L)
                .id("jti-logout")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();

        LogoutResponse response = userService.logout(current);

        assertThat(response.status()).isEqualTo("LOGGED_OUT");
        assertThat(revokedTokenRepository.existsByJti("jti-logout")).isTrue();
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }

        @Bean
        TokenService tokenService() {
            return (userId, subject) -> "test-token-" + subject;
        }

        @Bean
        PasswordRulesService passwordRulesService() {
            return new PasswordRulesService() {
                @Override
                public com.example.usermanagement.dto.PasswordRulesResponse getRules() { return null; }
                @Override
                public com.example.usermanagement.dto.PasswordRulesResponse updateRules(com.example.usermanagement.dto.PasswordRulesRequest request) { return null; }
                @Override
                public void validateOrThrow(String password) { }
            };
        }
    }
}
