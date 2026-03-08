package com.example.usermanagement.dto;

public record PasswordRulesResponse(
        int minLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSpecial,
        int minSpecialCount
) {
}
