package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetConfirmRequest(
        @NotBlank @Size(min = 20, max = 512) String resetToken,
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
