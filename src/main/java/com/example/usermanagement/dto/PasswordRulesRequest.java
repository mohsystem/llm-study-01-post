package com.example.usermanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PasswordRulesRequest(
        @Min(8) @Max(72) int minLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSpecial,
        @Min(0) @Max(5) int minSpecialCount
) {
}
