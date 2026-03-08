package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "username may only contain letters, numbers, and underscores")
        String username,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @Size(max = 20)
        @Pattern(regexp = "^$|^\\+?[1-9]\\d{7,14}$", message = "phoneNumber must be E.164 format")
        String phoneNumber,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
