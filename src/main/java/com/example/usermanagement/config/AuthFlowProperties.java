package com.example.usermanagement.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.auth")
public record AuthFlowProperties(
        @Min(1) @Max(120) long resetTokenTtlMinutes,
        @Min(1) @Max(30) long otpTtlMinutes,
        @Min(1) @Max(10) int otpMaxAttempts,
        @NotBlank String notificationEndpoint
) {
}
