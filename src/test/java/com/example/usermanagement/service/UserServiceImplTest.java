package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.RegistrationRequest;
import com.example.usermanagement.dto.RegistrationResponse;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.TestConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    @Test
    void registerShouldCreateUserWithHashedPassword() {
        RegistrationRequest request = new RegistrationRequest("secure_user", "secure@example.com", "StrongPass!123");

        RegistrationResponse response = userService.register(request);

        assertThat(response.accountId()).isNotNull();
        assertThat(response.status()).isEqualTo("REGISTERED");

        var created = userRepository.findByEmailIgnoreCase("secure@example.com").orElseThrow();
        assertThat(created.getPasswordHash()).isNotEqualTo("StrongPass!123");
        assertThat(passwordEncoder.matches("StrongPass!123", created.getPasswordHash())).isTrue();
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        userService.register(new RegistrationRequest("user_one", "duplicate@example.com", "StrongPass!123"));

        assertThatThrownBy(() -> userService.register(
                new RegistrationRequest("user_two", "duplicate@example.com", "StrongPass!123")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        userService.register(new RegistrationRequest("login_user", "login@example.com", "StrongPass!123"));

        LoginResponse response = userService.login(new LoginRequest("login_user", "StrongPass!123"));

        assertThat(response.sessionToken()).isEqualTo("test-token-login_user");
    }

    @Test
    void loginShouldRejectInvalidPasswordWithoutEnumeration() {
        userService.register(new RegistrationRequest("login_user2", "login2@example.com", "StrongPass!123"));

        assertThatThrownBy(() -> userService.login(new LoginRequest("login_user2", "WrongPass!123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        assertThatThrownBy(() -> userService.login(new LoginRequest("unknown_user", "WrongPass!123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
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
    }
}
