package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaChallengeRequest(
        @NotBlank
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "destination must be E.164 format")
        String destination
) {
}
