package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaVerifyRequest(
        @NotBlank String challengeId,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "otp must be 6 digits") String otp
) {
}
