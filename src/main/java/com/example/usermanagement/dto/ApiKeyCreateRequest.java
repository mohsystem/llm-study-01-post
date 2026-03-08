package com.example.usermanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApiKeyCreateRequest(
        @NotBlank @Size(min = 3, max = 80) String displayName,
        @Pattern(regexp = "^$|^[a-zA-Z0-9._-]{3,64}$", message = "serviceAccountRef contains invalid characters") String serviceAccountRef,
        @Min(1) @Max(365) int expiresInDays
) {
}
